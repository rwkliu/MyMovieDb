import Config from "backend/config.json";
import Axios from "axios";


/**
 * We use axios to create REST calls to our backend
 *
 * We have provided the login rest call for your
 * reference to build other rest calls with.
 *
 * This is an async function. Which means calling this function requires that
 * you "chain" it with a .then() function call.
 * <br>
 * What this means is when the function is called it will essentially do it "in
 * another thread" and when the action is done being executed it will do
 * whatever the logic in your ".then()" function you chained to it
 * @example
 * login(request)
 * .then(response => alert(JSON.stringify(response.data, null, 2)));
 */
async function login(loginRequest) {
    const requestBody = {
        email: loginRequest.email,
        password: loginRequest.password
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.baseUrl, // Base URL (localhost:8081 for example)
        url: Config.idm.login, // Path of URL ("/login")
        data: requestBody // Data to send in Body (The RequestBody to send)
    }

    return Axios.request(options);
}
export async function registerUser(registerRequest) {
    const requestBody = {
        email: registerRequest.email,
        password: registerRequest.password
    };

    const options = {
        method: "POST", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.baseUrl, // Base URL (localhost:8081 for example)
        url: Config.idm.register, // Path of URL ("/register")
        data: requestBody // Data to send in Body (The RequestBody to send)
    }
    return Axios.request(options);
}
export async function search(accessToken, searchRequest) {
    const requestBody = {
        title: searchRequest.title,
        year: searchRequest.year,
        director: searchRequest.director,
        genre: searchRequest.genre,
        limit: searchRequest.limit,
        orderBy: searchRequest.orderBy,
        direction: searchRequest.direction,
        page: searchRequest.page
    };

    const options = {
        method: "GET", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: "http://localhost:8082", // Base URL (localhost:8081 for example)
        url: "/movie/search", // Path of URL ("/register")
        //data: requestBody // Data to send in Body (The RequestBody to send)
        params: searchRequest, //input separate or one
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }
    return Axios.request(options);
}
export async function searchByMovieId(accessToken, id) {
    const options = {
        method: "GET", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: "http://localhost:8082", // Base URL (localhost:8081 for example)
        url: "/movie/" + id, // Path of URL ("/register")
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }
    return Axios.request(options);
}

export default {
    login, registerUser, search
}
