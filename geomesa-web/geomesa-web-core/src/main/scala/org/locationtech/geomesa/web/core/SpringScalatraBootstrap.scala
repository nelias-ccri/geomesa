/***********************************************************************
* Copyright (c) 2013-2015 Commonwealth Computer Research, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*************************************************************************/

package org.locationtech.geomesa.web.core

import javax.servlet.ServletContext
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper, HttpServletResponse}

import com.typesafe.scalalogging.slf4j.Logging
import org.apache.commons.lang.exception.ExceptionUtils
import org.locationtech.geomesa.accumulo.data.AccumuloDataStoreFactory
import org.scalatra.servlet.RichServletContext
import org.scalatra.{InternalServerError, ScalatraServlet}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}
import org.springframework.web.context.ServletContextAware

import scala.beans.BeanProperty
import scala.collection.JavaConversions._

trait GeoMesaScalatraServlet extends ScalatraServlet with Logging {

  @BeanProperty var debug: Boolean = false

  def root: String

  override def handle(req: HttpServletRequest, res: HttpServletResponse): Unit = req match {
    case r: HttpServletRequestWrapper => super.handle(r.getRequest.asInstanceOf[HttpServletRequest], res)
    case _ => super.handle(req, res)
  }

  /**
   * Pulls data store relevant values out of the request params
   */
  def datastoreParams: Map[String, String] =
    GeoMesaScalatraServlet.dsKeys.flatMap(k => params.get(k).map(k -> _)).toMap

  /**
   * Common error handler that accounts for debug setting
   */
  def handleError(msg: String, e: Exception) = {
    logger.error(msg, e)
    if (debug) {
      InternalServerError(reason = msg, body = s"${e.getMessage}\n${ExceptionUtils.getStackTrace(e)}")
    } else {
      InternalServerError()
    }
  }
}

object GeoMesaScalatraServlet {
  val dsKeys = new AccumuloDataStoreFactory().getParametersInfo.map(_.getName)
}

class SpringScalatraBootstrap extends ApplicationContextAware with ServletContextAware with Logging {

  @BeanProperty var applicationContext: ApplicationContext = _
  @BeanProperty var servletContext: ServletContext = _
  @BeanProperty var rootPath: String = _

  def init(): Unit = {
    val richCtx = new RichServletContext(servletContext)
    val servlets = applicationContext.getBeansOfType(classOf[GeoMesaScalatraServlet])
    for ((name, servlet) <- servlets) {
      val path = s"/$rootPath/${servlet.root}"
      logger.info(s"Mounting servlet bean '$name' at path '$path'")
      richCtx.mount(servlet, s"$path/*")
    }
  }
}
