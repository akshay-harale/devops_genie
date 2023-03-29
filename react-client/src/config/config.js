const host = window.location.hostname === 'localhost' ? 'http://localhost:8080' : "http://devops-genie-alb-746768237.us-west-2.elb.amazonaws.com";
const hostWS = window.location.hostname === 'localhost' ? 'ws://localhost:8080' : "ws://devops-genie-alb-746768237.us-west-2.elb.amazonaws.com";
// const host = "https://springboot-apis.herokuapp.com"
//window.location.hostname === 'localhost' ? 'http://localhost:8081' : ;
const config = {
    backendHost:  host,
    backendHostWS: hostWS
}

export default config