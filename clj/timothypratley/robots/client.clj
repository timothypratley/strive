(ns timothypratley.robots.client
  (:require [timothypratley.state-client :as protocol])
  (:require [timothypratley.swing-doctor :as sd]))

(def connection (ref nil))

(defn connect
  [host port]
  (dosync (ref-set connection (protocol/connect host port))))

(defn send-message
  [message]
  (protocol/send @connection message))

(defn login
  [username password]
  (send-message
    {:id :login
     :name username
     :password password}))

(defn -main
  [& args]
  (connect "localhost" 8888)
  (let [window (doto (sd/frame "Robots")
                 (full-screen)
                 (.setUndecorated true))]
    (sd/navigate window [login-screen nil])
    (stop-midi)))

(defn connect-screen
  [window _]
  (let [host (atom "localhost")
        port (atom "8888")]
    (sd/screen window blocker
               [[(sd/text-field host "Host:")
                 :gridx 1, :gridy 1]
                [(sd/text-field port "Port:")
                 :gridx 1, :gridy 2]
                [(sd/button "Connect" evt (.put blocker (connect @host @port)))
                 :gridx 1, :gridy 4]
                [(sd/button "Quit" evt (.put blocker :quit))
                 :gridx 1, :gridy 5]]
               [:quit nil
                

(defn login-screen
  [window _]
  (let [username (atom "")
        password (atom "")]
    (sd/screen window blocker
            [[(sd/text-field username "Username:")
              :gridx 1, :gridy 1]
             [(sd/password-field password "Password:")
              :gridx 1, :gridy 2]
             [(javax.swing.JLabel. "Hint: Conan/Barbarian")
              :gridx 2, :gridy 2]
             [(sd/button "Login" evt (.put blocker (login @username @password)))
              :gridx 1, :gridy 4]
             [(sd/button "Quit" evt (.put blocker :quit))
              :gridx 2, :gridy 5]]
            [:quit nil
             false login-screen
             character-select-screen])))




