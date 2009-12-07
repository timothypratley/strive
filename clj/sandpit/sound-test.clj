(ns sound-test
  (:import [javax.sound.sampled AudioSystem]
           [java.io File]))
 
(defn play-sound [in-path]
  (let [the-file (File. in-path)
        the-stream (AudioSystem/getAudioInputStream the-file)
        the-source-line (AudioSystem/getSourceDataLine (.getFormat the-stream))
        the-buffer-size (.getBufferSize the-source-line)
        the-buffer (make-array Byte/TYPE the-buffer-size)]
    (doto the-source-line (.open) (.start))
    (loop [num-read (.read the-stream the-buffer 0 the-buffer-size)]
      (loop [offset 0]
        (when (< offset num-read)
          (recur (.write the-source-line the-buffer offset (- num-read offset)))))
      (let [next-read (.read the-stream the-buffer 0 the-buffer-size)]
        (when (>= next-read 0)
          (recur next-read))))
    (doto the-source-line (.drain) (.stop))
    (.close the-stream)))
 
(play-sound "./farm.wav")

