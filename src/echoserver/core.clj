(ns echoserver.core
  (:require [cheshire.core :as json])
  (:gen-class))

(def msg-id (atom 1))

(defn gen-msg-id []
  (swap! msg-id inc))

(defn gen-reply-echo [msg]
  {:src (:dest msg)
   :dest (:src msg)
   :body {:msg_id (get-in msg [:body :msg_id])
          :in_reply_to (get-in msg [:body :msg_id])
          :type "echo_ok"
          :echo (get-in msg [:body :echo])}})

(defn gen-reply-init [msg]
  {:src (:dest msg)
   :dest (:src msg)
   :body {:msg_id (gen-msg-id)
          :in_reply_to (get-in msg [:body :msg_id])
          :type "init_ok"}})

(defn input-loop []
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (if (empty? line)
      nil
      (let [nline (json/decode line true)
            body-type (get-in nline [:body :type])
            node-id (get-in nline [:body :node_id])]
        (binding [*out* *err*]
          (println (str "Received " nline)))
        (cond
          (= body-type "init")  (do
                                  (binding [*out* *err*]
                                    (println (str "Initialized node " node-id)))
                                  (println (json/encode (gen-reply-init nline))))

          (= body-type "echo") (do
                                 (binding [*out* *err*]
                                   (println (str "Echoing " (:body nline))))
                                 (println (json/encode (gen-reply-echo nline)))))))))

(comment
  (input-loop))



(defn -main
  []
  (input-loop))

