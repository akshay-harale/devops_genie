import axios from 'axios';
import authHeader from './auth-header';
import config from "../config/config"

const API_URL = config.backendHost+'/users/';
const VIDEO_API_URL = config.backendHost+'/videos';

class UserService {
  getPublicContent() {
    return axios.get(API_URL + 'all');
  }

  getUserBoard() {
    return axios.get(API_URL + 'user', { headers: authHeader() });
  }
  
  getUserDetails(userId) {
    return axios.get(API_URL + userId, { headers: authHeader() });
  }

  getModeratorBoard() {
    return axios.get(API_URL + 'mod', { headers: authHeader() });
  }

  getAdminBoard() {
    return axios.get(API_URL + 'admin', { headers: authHeader() });
  }
  
  updateStatus(videoName, userId, duration) {
    return axios.patch(API_URL + "video_status", {
      videoName, 
      userId, 
      duration
    },{ headers: authHeader() });
  }

  getVideoStatus(videoName, userId){
    return axios.get(API_URL + 'video_status/'+userId+'/'+videoName, { headers: authHeader() });
  }
  uploadFile(formData) {
    console.log(formData)
    return axios.post(VIDEO_API_URL + "/upload", formData, { headers: authHeader() });
  }

  getVideos(){
    return axios.get(VIDEO_API_URL , { headers: authHeader() });
  }
}

export default new UserService();