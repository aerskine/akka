/**
 * Copyright 2010 Autodesk, Inc.  All rights reserved.
 * Licensed under Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 */

package se.scalablesolutions.akka.http.mist

import se.scalablesolutions.akka.actor. {ActorRef, Actor}

/**
 * @author Garrick Evans
 */

trait Endpoint
{
  this: Actor =>

  type Hook = Function[String, Boolean]
  type Provider = Function[String, ActorRef]

  /**
   * The list of connected endpoints to which this one should/could forward the request.
   * If the hook func returns true, the message will be sent to the actor returned from provider.
   */
  protected var _attachments = List[Tuple2[Hook, Provider]]()

  /**
   *
   */
  protected def _attach(hook:Hook, provider:Provider) =
    {
      _attachments = (hook, provider) :: _attachments
    }

  /**
   * Message handling common to all endpoints, must be chained
   */
  protected def _recv: Receive =
    {
      //
      // add the endpoint - the if the uri hook matches,
      // the message will be sent to the actor returned by the provider func
      //
      case Endpoint.Attach(hook, provider) => _attach(hook, provider)


      //
      // dispatch the suspended requests
      //
      case msg if msg.isInstanceOf[SuspendedRequest] =>
        {
          val req = msg.asInstanceOf[SuspendedRequest]
          val uri = req.request.getRequestURI
          val endpoints = _attachments.filter {_._1(uri)}

          if (endpoints.size > 0)
            endpoints.foreach {_._2(uri) ! req}
          else
            {
              self.sender match
              {
                case Some(s) => s reply Endpoint.NoneAvailable(uri, req)
                case None => _na(uri, req)
              }
            }
        }

    }

  /**
   * no endpoint available - completes the request with a 404
   */
  protected def _na(uri: String, req: SuspendedRequest) =
    {
      req.NotFound("No endpoint available for [" + uri + "]")
      log.debug("No endpoint available for [" + uri + "]")
    }
}


class RootEndpoint extends Actor with Endpoint
{
  final val Root = "/"

  //
  // use the configurable dispatcher
  //
  self.dispatcher = Endpoint.Dispatcher

  override def preStart = _attachments = Tuple2((uri: String) => {uri eq Root}, (uri: String) => self.asInstanceOf[ActorRef]) :: _attachments

  def recv: Receive =
    {
      case Endpoint.NoneAvailable(uri, req) => _na(uri, req)
      case unknown =>
        {
          log.error("Unexpected message sent to root endpoint. [" + unknown.toString + "]")
        }
    }

  /**
   * Note that root is a little different, other endpoints should chain their own recv first
   */
  def receive = {_recv orElse recv}
}



object Endpoint
{
  import se.scalablesolutions.akka.dispatch.Dispatchers


  /**
   * leverage the akka config to tweak the dispatcher for our endpoints
   */
  final val Dispatcher = Dispatchers.fromConfig("akka.rest.comet-dispatcher")

  type Hook = Function[String, Boolean]
  type Provider = Function[String, ActorRef]

  case class Attach(hook: Hook, provider: Provider)
  case class NoneAvailable(uri: String, req: SuspendedRequest)
}
