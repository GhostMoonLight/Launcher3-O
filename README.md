##github 上传项目
    1. 在GitHub上新建仓库
    2. 把github上面的仓库克隆到本地   git clone github项目地址
    3. 进入下载下来的文件夹  把本地项目文件夹下的所有文件copy到这个文件夹中
    4. 接下来依次输入以下代码即可完成其他剩余操作：
        git add .        （注：别忘记后面的.，此操作是把Test文件夹下面的文件都添加进来）
        git commit  -m  "提交信息" 
        git push -u origin master   （注：此操作目的是把本地仓库push到github上面）
    