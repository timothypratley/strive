(ns netty
  (:gen-class)
  (:import
     [java.net InetSocketAddress]
     [java.util.concurrent Executors]
     [org.jboss.netty.bootstrap ServerBootstrap]
     [org.jboss.netty.channel Channels ChannelPipelineFactory
                              SimpleChannelHandler]
     [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
     [org.jboss.netty.buffer ChannelBuffers]))
 
 
(defn send-msg
  "Sends string data to the channel of the client."
  [client msg]
  (.write client (ChannelBuffers/copiedBuffer msg "UTF-8")))
 
(defn make-handler
  "Returns a Netty handler."
  []
  (proxy [SimpleChannelHandler] []
    (channelConnected [ctx e]
      (let [c (.getChannel e)]
        (println "Connected:" c)))
 
    (channelDisconnected [ctx e]
      (let [c (.getChannel e)]
        (println "Disconnected:" c)))
 
    (messageReceived [ctx e]
      (let [c (.getChannel e)
            cb (.getMessage e)
            msg (.toString cb "UTF-8")]
        (println "Message:" msg "from" c)))
 
    (exceptionCaught
      [ctx e]
      (let [throwable (.getCause e)]
        (println "@exceptionCaught" throwable))
      (-> e .getChannel .close))))


(defn start
  "Start a Netty server. Returns the pipeline."
  [port handler]
  (let [channel-factory (NioServerSocketChannelFactory.
                          (Executors/newCachedThreadPool)
                          (Executors/newCachedThreadPool))
        bootstrap (ServerBootstrap. channel-factory)
        pipeline (.getPipeline bootstrap)]
    (.addLast pipeline "handler" handler)
    (.setOption bootstrap "child.tcpNoDelay", true)
    (.setOption bootstrap "child.keepAlive", true)
    (.bind bootstrap (InetSocketAddress. port))
    pipeline))


