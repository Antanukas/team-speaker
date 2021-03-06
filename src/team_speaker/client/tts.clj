(ns team-speaker.client.tts
  (:require
    [team-speaker.core.context :as ctx]
    [team-speaker.client.player :as player]
    [clj-http.client :as client]
    [clojure.java.io :as io]))
(taoensso.timbre/refer-timbre)

(defn- speech-to-file! [url params client-f]
  (let [tmp-dir (:tmp-dir @ctx/config)
        file-name (str (java.util.UUID/randomUUID) ".mp3")
        file-path (str tmp-dir "/" file-name)
        use-proxy? (:http-use-proxy @ctx/config)
        proxy-cfg (if use-proxy? {:proxy-host (:http-proxy-host @ctx/config) :proxy-port (:http-proxy-port @ctx/config)} {})
        request-params (merge proxy-cfg params {:as :stream})]
    (debug "Speech to file url " url " params: " request-params)
    (with-open [body-stream (:body (client-f url request-params))]
      (io/copy body-stream (io/file file-path)))
    file-path))

(defn google-speech-to-file! [phrase]
  (let [language (:google-tts-language @ctx/config)
        ;Google limits to 100 chars only :/
        phrase-truncated (apply str (take 100 phrase))]
    (info "Speaking: " phrase-truncated)
    (speech-to-file! (:google-tts-url @ctx/config)
                     {:query-params {:tl language :q phrase-truncated}}
                     (partial client/get))))

(defn acapela-speech-to-file! [phrase]
  (let [url (:acapela-tts-url @ctx/config)
        form-params {:form-params {:req_asw_type "STREAM" :cl_login "EVAL_VAAS"
                                   :cl_app (:acapela-tts-usr @ctx/config)
                                   :cl_pwd (:acapela-tts-psw @ctx/config)
                                   :req_voice "willlittlecreature22k"
                                   :req_text phrase :req_snd_type "MP3"}}]
    (speech-to-file! url form-params (partial client/post))))