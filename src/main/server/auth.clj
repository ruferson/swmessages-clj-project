(ns server.auth
  (:require [buddy.hashers :as hashers]
            [clojure.string :as str])
  (:import [java.sql DriverManager Connection PreparedStatement ResultSet]))

(def db-url "jdbc:sqlite:holonet_users.db")

(defonce users-db (atom nil))

(defn- db []
  (or @users-db
      (let [conn (DriverManager/getConnection db-url "sa" "")]
        (with-open [stmt (.prepareStatement conn "CREATE TABLE IF NOT EXISTS users (email TEXT PRIMARY KEY, id TEXT NOT NULL, name TEXT NOT NULL, password TEXT NOT NULL, rank TEXT, photo_url TEXT, initials TEXT, created_at INTEGER NOT NULL)")]
          (.executeUpdate stmt))
        (reset! users-db conn)
        conn)))

(defn- user-from-rs [^ResultSet rs]
  {:id (.getString rs "id")
   :name (.getString rs "name")
   :email (.getString rs "email")
   :password (.getString rs "password")
   :rank (.getString rs "rank")
   :photo-url (.getString rs "photo_url")
   :initials (.getString rs "initials")
   :created-at (.getLong rs "created_at")})

(defn- fetch-users []
  (with-open [stmt (.prepareStatement (db) "SELECT * FROM users")
             rs (.executeQuery stmt)]
    (loop [users []]
      (if (.next rs)
        (recur (conj users (user-from-rs rs)))
        users))))

(defn- fetch-user-by-email [email]
  (with-open [stmt (.prepareStatement (db) "SELECT * FROM users WHERE email = ?")]
    (.setString stmt 1 email)
    (let [rs (.executeQuery stmt)]
      (if (.next rs)
        (user-from-rs rs)
        nil))))

(defn- insert-user! [{:keys [id name email password rank photo-url initials created-at]}]
  (with-open [stmt (.prepareStatement (db)
                                   "INSERT INTO users (email, id, name, password, rank, photo_url, initials, created_at)
                                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)")]
    (.setString stmt 1 email)
    (.setString stmt 2 id)
    (.setString stmt 3 name)
    (.setString stmt 4 password)
    (.setString stmt 5 (or rank ""))
    (.setString stmt 6 photo-url)
    (.setString stmt 7 (or initials ""))
    (.setLong stmt 8 (or created-at 0))
    (.executeUpdate stmt)))

(defn- email-valid? [email]
  (and (string? email)
       (re-matches #".+@.+\..+" email)))

(defn register-user [{:keys [name email password password-confirmation rank photo-url]}]
  (let [name-clean (str/trim (or name ""))
        email-clean (str/lower-case (str/trim (or email "")))]
    (cond
      (str/blank? name-clean)
      {:status 400 :body {:success false :message "El identificador de operador es obligatorio"}}

      (not (email-valid? email-clean))
      {:status 400 :body {:success false :message "Formato de frecuencia/correo galáctico no válido"}}

      (some #(= (:email %) email-clean) (fetch-users))
      {:status 409 :body {:success false :message "El correo galáctico ya se encuentra registrado en el sistema"}}

      (or (str/blank? password) (< (count password) 6))
      {:status 400 :body {:success false :message "El código de encriptación debe tener al menos 6 caracteres"}}

      (not= password password-confirmation)
      {:status 400 :body {:success false :message "Los códigos de encriptación no coinciden"}}

      :else
      (let [password-hashed (hashers/derive password)
            new-user {:id (str "op-" (System/currentTimeMillis))
                      :name name-clean
                      :email email-clean
                      :password password-hashed
                      :rank (if (str/blank? rank) "Oficial de Enlace" rank)
                      :photo-url photo-url
                      :initials (str/upper-case (subs name-clean 0 (min 2 (count name-clean))))
                      :created-at (System/currentTimeMillis)}]
        (insert-user! new-user)
        {:status 201
         :body {:success true
                :message "Operador registrado en frecuencia correctamente"
                :user (dissoc new-user :password)}}))))

(defn login-user [{:keys [email password]}]
  (let [email-clean (str/lower-case (str/trim (or email "")))
        user (fetch-user-by-email email-clean)]
    (if (and user (hashers/check password (:password user)))
      {:status 200
       :body {:success true
              :message "Enlace de frecuencia establecido correctamente"
              :user (dissoc user :password)}}
      {:status 401
       :body {:success false
              :message "Frecuencia o código de encriptación inválidos"}})))
