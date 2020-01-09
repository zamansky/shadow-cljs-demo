(ns main
  (:require [reagent.core :as r]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]
            ))

(def counter (r/atom 0))

(def event-queue (chan))

(go-loop [ [event payload] (<! event-queue)]
  (case event
    :inc (swap! counter #(+ % payload))
    :dec (swap! counter #(- % payload))
    )
  (recur (<! event-queue))
  )

(defn main-component []
  [:div 
   [:h1.text-2xl.font-bold "This is a component from cljs"]
   [:h1.text-4xl.font-bold.p-4 {
                                :class (if (< @counter 10)
                                         "bg-green-400"
                                         "bg-red-400")
                                :on-click #(put! event-queue [:inc 1])} @counter]
   
   (into [:ol.p-4] (for [item (range @counter)] [:li item]))
   ]
  )


(defn mount [c]
(r/render-component [c] (.getElementById js/document "app"))
)

(defn reload! []
(mount main-component)
(print "Hello reload!"))

(defn main! []
(mount main-component)
(print "Hello Main"))
