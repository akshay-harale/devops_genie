const host = window.location.hostname === 'localhost' ? 'http://localhost:8080' : "https://springboot-apis.herokuapp.com";
// const host = "https://springboot-apis.herokuapp.com"
//window.location.hostname === 'localhost' ? 'http://localhost:8081' : ;
const config = {
    backendHost: host
}

export default config