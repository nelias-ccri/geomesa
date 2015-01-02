package org.locationtech.geomesa.kafka

import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.Properties
import java.{util => ju}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.eventbus.EventBus
import kafka.consumer.{Consumer, ConsumerConfig, Whitelist}
import kafka.message.MessageAndMetadata
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.serializer.DefaultDecoder
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.commons.lang3.RandomStringUtils
import org.geotools.data.DataAccessFactory.Param
import org.geotools.data.store.{ContentDataStore, ContentEntry, ContentFeatureSource}
import org.geotools.data.{AbstractDataStoreFactory, DataStore}
import org.geotools.feature.NameImpl
import org.locationtech.geomesa.feature.AvroFeatureDecoder
import org.locationtech.geomesa.utils.geotools.SimpleFeatureTypes
import org.opengis.feature.`type`.Name
import org.opengis.feature.simple.SimpleFeatureType

class KafkaDataStore(broker: String, zookeepers: String, isProducer: Boolean)
  extends ContentDataStore {
  import scala.collection.JavaConverters._

  private val groupId = RandomStringUtils.randomAlphanumeric(5)
  val zkClient = new ZkClient(zookeepers)

  override def createTypeNames() =
    ZkUtils.getAllTopics(zkClient)
      .map(t => new NameImpl(t)).toList.asJava.asInstanceOf[java.util.List[Name]]

  private val schemaKey = "schema".getBytes(StandardCharsets.UTF_8)
  override def createSchema(featureType: SimpleFeatureType) = {
    val topic = featureType.getTypeName
    if(getTypeNames.contains(topic)) throw new IllegalArgumentException(s"Typename already taken")
    else {
      //AdminUtils.createTopic(zkClient, topic, 1, 1, new Properties())
      val props = new Properties()
      props.put("metadata.broker.list", broker)
      props.put("serializer.class", "kafka.serializer.DefaultEncoder")
      props.put("request.required.acks", "1")
      props.put("producer.type", "sync")
      val kafkaProducer = new Producer[Array[Byte], Array[Byte]](new ProducerConfig(props))
      val encodedSchema = SimpleFeatureTypes.encodeType(featureType).getBytes(StandardCharsets.UTF_8)
      val schemaMsg = new KeyedMessage[Array[Byte], Array[Byte]](topic, schemaKey, encodedSchema)
      kafkaProducer.send(schemaMsg)
      kafkaProducer.close()
    }
  }

  override def createFeatureSource(entry: ContentEntry) =
    if(isProducer) createProducerFeatureSource(entry)
    else createConsumerFeatureSource(entry)

  def createProducerFeatureSource(entry: ContentEntry): ContentFeatureSource =
    new KafkaProducerFeatureStore(entry, schemaCache.get(entry.getTypeName), broker, null)

  def createConsumerFeatureSource(entry: ContentEntry): ContentFeatureSource = {
    if (createTypeNames().contains(entry.getName)) {
      val topic = entry.getTypeName
      val eb = new EventBus(topic)
      val sft = schemaCache.get(topic)
      val groupId = RandomStringUtils.randomAlphanumeric(5)
      val decoder = new AvroFeatureDecoder(sft)
      val producer =
        new KafkaFeatureConsumer(topic, zookeepers, groupId, decoder, eb)
      new KafkaConsumerFeatureSource(entry, sft, eb, producer, null)
    } else null
  }

  val schemaCache =
    CacheBuilder.newBuilder().build(new CacheLoader[String, SimpleFeatureType] {
      override def load(k: String): SimpleFeatureType = resolveTopicSchema(k)
    })

  def resolveTopicSchema(topic: String): SimpleFeatureType = {
    val client = Consumer.create(new ConsumerConfig(buildClientProps))
    val whitelist = new Whitelist(topic)
    val keyDecoder = new DefaultDecoder(null)
    val valueDecoder = new DefaultDecoder(null)
    val stream =
      client.createMessageStreamsByFilter(whitelist, 1, keyDecoder, valueDecoder).head

    val iter = stream.iterator()
    iter.dropWhile { case msg: MessageAndMetadata[Array[Byte], Array[Byte]] =>
      msg.key() == null || !msg.key().equals(schemaKey)
    }
    val spec = stream.iterator().next().message()
    SimpleFeatureTypes.createType(topic, new String(spec, StandardCharsets.UTF_8))
  }

  private def buildClientProps = {
    val props = new Properties()
    props.put("zookeeper.connect", zookeepers)
    props.put("group.id", groupId)
    props.put("zookeeper.session.timeout.ms", "2000")
    props.put("zookeeper.sync.time.ms", "1000")
    props.put("auto.commit.interval.ms", "1000")
    props.put("auto.offset.reset", "smallest")
    props
  }

}

class KafkaDataStoreFactory extends AbstractDataStoreFactory {

  val KAFKA_BROKER_PARAM = new Param("broker", classOf[String], "Kafka broker", true)
  val ZOOKEEPERS_PARAM   = new Param("zookeepers", classOf[String], "Zookeepers", true)
  val IS_PRODUCER_PARAM  = new Param("isProducer", classOf[java.lang.Boolean], "Is Producer", true)

  override def createDataStore(params: ju.Map[String, Serializable]): DataStore = {
    val broker   = KAFKA_BROKER_PARAM.lookUp(params).asInstanceOf[String]
    val zk       = ZOOKEEPERS_PARAM.lookUp(params).asInstanceOf[String]
    val producer = IS_PRODUCER_PARAM.lookUp(params).asInstanceOf[java.lang.Boolean]
    new KafkaDataStore(broker, zk, producer)
  }

  override def createNewDataStore(params: ju.Map[String, Serializable]): DataStore = ???

  override def getDescription: String = ???

  override def getParametersInfo: Array[Param] = Array(KAFKA_BROKER_PARAM, ZOOKEEPERS_PARAM)
}