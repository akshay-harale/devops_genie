import axios from "axios"
import config from "../config/config"
import { getAuth, createUserWithEmailAndPassword,signInWithEmailAndPassword } from "firebase/auth";

const API_URL = config.backendHost+"/api/auth/";

class AuthService {
    login(username, password) {
        signInWithEmailAndPassword(auth, username, password)
        .then((userCredential) => {
            // Signed in 
            const user = userCredential.user;
            console.log(userCredential);
            localStorage.setItem("user", user.email);
            return user;
        })
        .catch((error) => {
            return error;
        });
    }

    logout() {
        localStorage.removeItem("user");
    }
    register(username, email, password) {

        const auth = getAuth();
        createUserWithEmailAndPassword(auth, email, password)
        .then((userCredential) => {
          // Signed in 
          const user = userCredential.user;
          return user;
        })
        .catch((error) => {
          const errorCode = error.code;
          const errorMessage = error.message;
          return errorMessage;
        });

        // return axios.post(API_URL + "signup", {
        //     username,
        //     email,
        //     password
        // });
    }

    getCurrentUser() {
        return localStorage.getItem('user');
    }
}

export default new AuthService()