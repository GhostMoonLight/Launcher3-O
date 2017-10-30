git学习：http://blog.csdn.net/u011116672/article/details/51233837
##github 上传项目
    1. 在GitHub上新建仓库
    2. 把github上面的仓库克隆到本地   git clone github项目地址
    3. 进入下载下来的文件夹  把本地项目文件夹下的所有文件copy到这个文件夹中
    4. 接下来依次输入以下代码即可完成其他剩余操作：
        git add .        （注：别忘记后面的.，此操作是把Test文件夹下面的文件都添加进来）
        git commit  -m  "提交信息" 
        git push -u origin master   （注：此操作目的是把本地仓库push到github上面）
    
    
##提交更新
    1. 先添加更新的文件  git add .
    2. 然后提交 git commit -m 注释
    3. 每次提交到远程之前应先拉取最新的提交，git pull
    4. 提交到远程 git pull
        如果发生冲突会导致提交失败：
            git ls-files -s    // 查看暂存区中记录的冲突文件
                100755 d46af2ef8d4e803e179dc93bc8e1c47e2452bea5 1	README.md
                100755 6651bd6e9e702ebf78e7695a08e421fca56d7674 2	README.md
                100755 c88d6c003342ad3c305b8b773cab15762da8792f 3	README.md
            编号为1 是原始的信息
            编号为2 是暂存区用于保存冲突文件在当前分支中修改的副本
            编号为3 是暂存区用于保存当前冲突文件在远程版本中的副本
            git show :3:README.md  查看该文件的内容
            
            cat README.md  查看工作区的README.md的内容
            其中特殊标识<<<<<<<和=======之间的内容是当前分支所更新的内容，
            特殊标识=======和>>>>>>>之间的内容是所合并的版本更改的内容。
            所以只需要手动解决这个冲突就可以，将README.txt文件修改如下：
            
            解决玩冲突之后，执行git add -u 加上-u参数表示把工作区被跟踪的文件添加到暂存区
            然后执行commit提交，然后push
                      
            
            