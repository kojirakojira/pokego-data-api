# pokego-data-api概要

そのうち書く。

## 環境構築方法

前提として、このプロジェクトでは以下2つの環境変数を設定する必要がある。

- REDIS_URL
    - 例：redis://h:{password}@localhost:6379
    - Redis用のURLを指定する。username,passwordは省略可能。usernameに"h"を指定した場合は無視される。（Redisのver3, 4らへんの仕様上そうしている。）
- SPRING_PROFILES_ACTIVE（省略可能）
    - staging または productionを指定する。
    - 指定しない場合はデフォルトのappliation.ymlを参照する。

### ローカル環境

ローカル環境では、起動方法が2つある。

#### Spring Bootプロジェクトとして起動する方法

IDE依存のため割愛。

#### Dockerで起動する方法

DockerはMavenを使用して起動する構成にしている。詳細はDockerfileを参照。<br>
※ 以下手順において、RedisはローカルPC上、もしくはインターネット上で起動する前提である。（ローカルのDocker上では起動しない。）

##### 1.ビルド

--build-argオプションで、REDIS_URLを指定する。
```
cd {プロジェクトのルートディレクトリ}
docker build . --build-arg REDIS_URL=redis://h:76b6a920@host.docker.internal:6379 -t pokego-data-api:latest
```

##### 2.コンテナ起動

```
docker run --add-host=host.docker.internal:host-gateway -it {イメージID}
```

##### Tips

###### dockerの起動中のイメージ一覧表示
```
docker image ls
```

###### dockerイメージの削除
```
docker image rm {イメージID}
```


###### 起動中のdockerコンテナの一覧
```
docker ps -a
```


###### コンテナの削除
```
docker rm {コンテナID}
```



###### コンテナ内にsshしてコマンドを実行(以下、lsの例)
```
docker exec -it {コンテナID} ls
```

### Docker, Render.com

あとで書く。