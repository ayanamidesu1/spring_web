export class BaseApi {
    private base_url: string
    private token: string
    public s_base_url: string
    constructor(base_url: string = 'https://www.sunyuanling1.com/api/') {
        this.base_url = base_url;
        this.token = this.get_token();
        this.s_base_url = 'https://www.sunyuanling1.com/server/static/'
    }
    private get_token(): string {
        return localStorage.getItem('token') || ''
    }

    public async post(api = '', data: Object): Promise<{ result: Object, status: number }> {
        console.log(this.get_token())
        try {
            const token = this.get_token()
            const res = await fetch(this.base_url + api, {
                method: 'POST',
                headers: {
                    'Authorization': `token ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
            const result = await res.json()
            const status = res.status
            return { result, status }
        }
        catch (err) {
            console.log(err)
            return { result: {}, status: 500 }
        }

    }
    //一些常用的api
    //获取用户对于目标用户的关注状态
    public async get_follow_status(user_id: string): Promise<Boolean> {
        let res = await this.post('GetUserInfo/GetUserFollowStatus', {
            target_id: user_id
        })
        console.log(res)
        if (res.status == 200) {
            if (res.result.status == 0) {
                return false
            }
            if (res.result.status == 1) {
                return true
            }
        }
        return false
    }
    public async get_self_info(): Promise<{ result: Object, status: number }> {
        return this.post('GetUserInfo/GetSelfInfo', {

        })
    }
    //新增关注
    public async add_follow(user_id: string): Promise<{ result: Object, status: number }> {
        let res = await this.post('GetUserInfo/UserAddFollow/', {
            target_id: user_id
        })
        return res.result
    }

    public async verify_login(): Promise<{ result: Object, status: number }> {
        return this.post('verify/', {})
    }
    //通用时间格式化函数
    public formatTimeAgo(dateTimeInput: string | Date | number): string {
        try {
            // 参数类型转换 --------------------------------------
            let dateTimeStr: string;
            
            if (typeof dateTimeInput === 'string') {
                dateTimeStr = dateTimeInput;
            } else if (dateTimeInput instanceof Date) {
                dateTimeStr = dateTimeInput.toISOString();
            } else if (typeof dateTimeInput === 'number') {
                dateTimeStr = new Date(dateTimeInput).toISOString();
            } else {
                return '刚刚';
            }
    
            // 标准化时间格式 ------------------------------------
            const standardized = dateTimeStr
                .replace('T', ' ')
                .replace(/\.\d+Z?$/, (match) => {
                    const millis = match.slice(1, 4).padEnd(3, '0');
                    return `.${millis}`;
                });
    
            // 日期有效性校验 ------------------------------------
            const targetTime = new Date(standardized);
            if (isNaN(targetTime.getTime())) {
                return '刚刚';
            }
    
            // 时间差计算 ----------------------------------------
            const now = new Date();
            const diff = now.getTime() - targetTime.getTime();
            const seconds = Math.floor(diff / 1000);
    
            // 超过30天显示具体日期 -------------------------------
            if (diff > 30 * 24 * 3600 * 1000) {
                return `${targetTime.getFullYear()}年${
                    targetTime.getMonth() + 1}月${
                    targetTime.getDate()}日`;
            }
    
            // 时间单位计算 --------------------------------------
            const intervals = {
                年: 31536000,
                月: 2592000, // 30天
                周: 604800,
                天: 86400,
                小时: 3600,
                分钟: 60,
                秒: 1
            };
    
            for (const [unit, secondsInUnit] of Object.entries(intervals)) {
                const counter = Math.floor(seconds / secondsInUnit);
                if (counter > 0) {
                    return `${counter}${unit}前`;
                }
            }
    
            return '刚刚';
        } catch (err) {
            console.error('时间格式化失败:', err);
            return '刚刚';
        }
    }
    
    public change_img(node: HTMLImageElement, file: File): Promise<string> {
        return new Promise((resolve, reject) => {
            // 增强类型校验
            if (!(node instanceof HTMLImageElement)) {
                return reject(new Error('Invalid image element'))
            }
            if (!(file instanceof File)) {
                return reject(new Error('Invalid File object'))
            }
            // 扩展文件类型校验
            const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
            if (!ALLOWED_TYPES.includes(file.type)) {
                return reject(new Error(`Unsupported image type: ${file.type}`))
            }
            // 内存管理
            const revokePreviousURL = () => {
                if (node.src.startsWith('blob:')) {
                    URL.revokeObjectURL(node.src)
                }
            }
            const reader = new FileReader()
            reader.onload = (e) => {
                revokePreviousURL()
                const result = e.target?.result
                if (typeof result !== 'string') {
                    return reject(new Error('Failed to read file as DataURL'))
                }
                // 添加临时加载状态
                node.style.opacity = '0.5'
                const handleLoad = () => {
                    node.style.opacity = '1'
                    resolve(result)
                }
                const handleError = () => {
                    node.style.opacity = '1'
                    reject(new Error('Failed to load image'))
                }
                node.addEventListener('load', handleLoad, { once: true })
                node.addEventListener('error', handleError, { once: true })
                node.src = result
            }
            reader.onerror = () => {
                reject(new Error(`File read error: ${reader.error?.message}`))
            }
            try {
                reader.readAsDataURL(file)
            } catch (error) {
                reject(new Error(`File read failed: ${error}`))
            }
        })
    }

}