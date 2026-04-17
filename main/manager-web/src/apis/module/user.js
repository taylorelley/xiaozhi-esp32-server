import { getServiceUrl } from '../api'
import RequestService from '../httpRequest'


export default {
    // Log in
    login(loginForm, callback, failCallback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/login`)
            .method('POST')
            .data(loginForm)
            .success((res) => {
                RequestService.clearRequestTime()
                callback(res)
            })
            .fail((err) => {
                RequestService.clearRequestTime()
                failCallback(err)
            })
            .networkFail(() => {
                RequestService.reAjaxFun(() => {
                    this.login(loginForm, callback)
                })
            }).send()
    },
    // GetCaptcha
    getCaptcha(uuid, callback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/captcha?uuid=${uuid}`)
            .method('GET')
            .type('blob')
            .header({
                'Content-Type': 'image/gif',
                'Pragma': 'No-cache',
                'Cache-Control': 'no-cache'
            })
            .success((res) => {
                RequestService.clearRequestTime();
                callback(res);
            })
            .networkFail((err) => {  // Add error parameter

            }).send()
    },
 // Captcha
    sendSmsVerification(data, callback, failCallback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/smsVerification`)
            .method('POST')
            .data(data)
            .success((res) => {
                RequestService.clearRequestTime()
                callback(res)
            })
            .fail((err) => {
                RequestService.clearRequestTime()
                failCallback(err)
            })
            .networkFail(() => {
                RequestService.reAjaxFun(() => {
                    this.sendSmsVerification(data, callback, failCallback)
                })
            }).send()
    },
 // Register
    register(registerForm, callback, failCallback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/register`)
            .method('POST')
            .data(registerForm)
            .success((res) => {
                RequestService.clearRequestTime()
                callback(res)
            })
            .fail((err) => {
                RequestService.clearRequestTime()
                failCallback(err)
            })
            .networkFail(() => {
                RequestService.reAjaxFun(() => {
                    this.register(registerForm, callback, failCallback)
                })
            }).send()
    },
    // SaveDeviceConfiguration
    saveDeviceConfig(device_id, configData, callback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/configDevice/${device_id}`)
            .method('PUT')
            .data(configData)
            .success((res) => {
                RequestService.clearRequestTime();
                callback(res);
            })
            .networkFail((err) => {
                console.error('SaveConfigurationfailed:', err);
                RequestService.reAjaxFun(() => {
                    this.saveDeviceConfig(device_id, configData, callback);
                });
            }).send();
    },
    // User infoGet
    getUserInfo(callback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/info`)
            .method('GET')
            .success((res) => {
                RequestService.clearRequestTime()
                callback(res)
            })
            .networkFail((err) => {
                console.error('APIRequest failed:', err)
                RequestService.reAjaxFun(() => {
                    this.getUserInfo(callback)
                })
            }).send()
    },
    // ModifyUserPassword
    changePassword(oldPassword, newPassword, successCallback, errorCallback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/change-password`)
            .method('PUT')
            .data({
                password: oldPassword,
                newPassword: newPassword,
            })
            .success((res) => {
                RequestService.clearRequestTime();
                successCallback(res);
            })
            .networkFail((error) => {
                RequestService.reAjaxFun(() => {
                    this.changePassword(oldPassword, newPassword, successCallback, errorCallback);
                });
            })
            .send();
    },
    // ModifyUserStatus
    changeUserStatus(status, userIds, successCallback) {
        console.log(555, userIds)
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/admin/users/changeStatus/${status}`)
            .method('put')
            .data(userIds)
            .success((res) => {
                RequestService.clearRequestTime()
                successCallback(res);
            })
            .networkFail((err) => {
                console.error('ModifyUserStatusfailed:', err)
                RequestService.reAjaxFun(() => {
                    this.changeUserStatus(status, userIds)
                })
            }).send()
    },
    // GetPublicConfiguration
    getPubConfig(callback, failCallback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/pub-config`)
            .method('GET')
            .success((res) => {
                RequestService.clearRequestTime();
                callback(res);
            })
            .fail((err) => {
                RequestService.clearRequestTime();
                if (failCallback) {
                    failCallback(err);
                }
            })
            .networkFail((err) => {
                console.error('GetPublicConfigurationfailed:', err);
                RequestService.reAjaxFun(() => {
                    this.getPubConfig(callback, failCallback);
                });
            }).send();
    },
 // UserPassword
    retrievePassword(passwordData, callback, failCallback) {
        RequestService.sendRequest()
            .url(`${getServiceUrl()}/user/retrieve-password`)
            .method('PUT')
            .data({
                phone: passwordData.phone,
                code: passwordData.code,
                password: passwordData.password,
                captchaId: passwordData.captchaId
            })
            .success((res) => {
                RequestService.clearRequestTime();
                callback(res);
            })
            .fail((err) => {
                RequestService.clearRequestTime();
                failCallback(err);
            })
            .networkFail(() => {
                RequestService.reAjaxFun(() => {
                    this.retrievePassword(passwordData, callback, failCallback);
                });
            }).send()
    },

}
