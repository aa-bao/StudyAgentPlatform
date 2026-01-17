import request from '../utils/request'

// 登录
export function loginApi(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 注册
export function registerApi(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

// 更新个人信息
export function updateUserApi(data) {
    return request({
        url: '/user/update',
        method: 'post',
        data
    })
}

// 修改密码
export function updatePwdApi(data) {
    return request({
        url: '/user/updatePwd',
        method: 'post',
        data
    })
}

// 上传头像
export function uploadAvatarApi(formData) {
    return request({
        url: '/file/upload', 
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取当前登录用户的详细信息
export function getUserInfoApi() {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

// 获取用户首页数据
export function getHomePageDataApi(userId) {
    return request({
        url: `/user/homeData/${userId}`,
        method: 'get'
    })
}
