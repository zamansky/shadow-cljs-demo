(ns main
  (:require [reagent.core :as r]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]
            ))

;;-----------------------  State / Events ----------------------------------------

(def counter (r/atom 0))

(def event-queue (chan))

(go-loop [ [event payload] (<! event-queue)]
  (case event
    :inc (swap! counter #(+ % payload))
    :dec (swap! counter #(- % payload))
    :login (prn payload)
    )
  (recur (<! event-queue))
  )

;;-----------------------  Components ----------------------------------------

(defn input-box [type label var]
  [:div.input-box
   [:label label]
   [:input {:on-change #(reset! var (-> % .-target .-value))
            :type type}]]
  
  )
(defn login-box []
  (let [username (r/atom "")
        password (r/atom "")
        ]
    [:div
     [input-box "text" "Username: " username]
     [input-box "password" "Password: " password]
     [:button.btn-blue.hover:bg-teal-400
      {:on-click #(put! event-queue [:login [@username @password]])} "press-me"]]
    ))

(defn navbar []
  [:div.flex.bg-black.w-full.text-white.p-2.mb-2
   [:a.m-2.px-3.py-2.border-2 {:href "#"} "HOME"]
   [:a.m-2.px-3.py-2.border-2 {:href "#about"} "ABOUT"]
   [:a.m-2.px-3.py-2.border-2 {:href "#help"} "HELP"]
   ]
  )

;;-----------------------  Pages ----------------------------------------

(defn about-page []
  [:div
   [navbar]
   [:h1.text-4xl.font-bold "This about page"]
   ])

(defn help-page []
  [:div
   [navbar]
   [:h1.text-4xl.font-bold "This help page"]
   ])

(defn main-page []
  [:div
   [navbar]
   [:h1.text-2xl.font-bold "This is a component from cljs"]
   [:h1.text-4xl.font-bold.p-4 {
                                :class (if (< @counter 10)
                                         "bg-green-400"
                                         "bg-red-400")
                                :on-click #(put! event-queue [:inc 1])} @counter]

   ;;(into [:ol.p-4] (for [item (range @counter)] [:li item]))
   [login-box]
   ]
  )


;;-----------------------  Utilities ----------------------------------------

(defn mount [c]
  (r/render-component [c] (.getElementById js/document "app"))
  )

(def routes
  {"#about" about-page
   "#help" help-page
   "" main-page
   "default" about-page
   })

(defn handleroutes [routes event]
  (let [loc (.-location.hash js/window)
        ;; pathname (.-location.pathname js/window)
        newpage (get routes loc (get routes "default"))
        ]
    (.history.replaceState js/window {} nil loc)
    (mount newpage)
    )
  )

(defn setup-router [routes]
  (.addEventListener js/window "hashchange" #(handleroutes routes %))
  (handleroutes routes nil)
  )

(defn reload! []
  (setup-router routes)
  (print "Hello reload!"))

(defn main! []
  (setup-router routes)
  (print "Hello Main"))
