# pokego-data-api概要

そのうち書く。

## 環境構築方法

前提として、このプロジェクトでは以下の環境変数を設定する。

- REDIS_URL（省略可能）
  1. 例：redis://h:{password}@localhost:6379
  1. Redis用のURLを指定する。指定しなかった場合、LettuceConnectionFactoryの仕様で、localhost:6379に接続する。
- DATABASE_URL
  1. 以下を設定する。"jdbc:"は無くても良い。クエリパラメータでの指定も可能。<br> postgres://\<username\>:\<password\>@\<hostname\>:\<port\>/\<dbname\>
- DATABASE_USERNAME（省略可能）
  1. 設定した場合、優先的に参照される。（DATABASE_URL内のusernameは無視される。）
- DATABASE_PASSWORD（省略可能）
  1. 設定した場合、優先的に参照される。（DATABASE_URL内のpasswordは無視される。）
- S3_ACCESS_KEY_ID
  1. AWS S3サーバにアクセス可能なIAMユーザのアクセスキーを指定する。
- S3_SECRET_ACCESS_KEY
  1. AWS S3サーバにアクセス可能なIAMユーザのシークレットアクセスキーを指定する。
- SPRING_PROFILES_ACTIVE（省略可能）
  1. staging または productionを指定する。
  1. 指定しない場合はデフォルトのappliation.ymlを参照する。
- JWT_UUID（省略可能）
  1. UUIDを生成して指定する。現状、JWT認証を使用する機能はまだ設けていないため設定しなくてもOK。
- BRAINJUICE_NOW_DATE（省略可能）
  1. 日付を変更したい場合に指定する。
  1. フォーマット："yyyy-MM-dd HH:mm:ss"

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

## Redisサーバに管理者権限のユーザを追加する方法
1. HSETでuseIdを登録する。_classはちゃんとパッケージを合わせる
1. SADD admins #{ユーザID}
1. HSET admins:#{ユーザID} userId #{ユーザID}

### Tips

@RedisHashで定義したものは、Redis上で、SETとHASHの2つを使用して管理される。

1. KEYS admins* // 存在しているキーの確認
1. SMEMBERS key // SETのキーを確認
1. SADD key // SETが存在しない(adminsが存在しない)場合は、HADDでキーを追加する。
1. HSET key field value // HASHが存在しない(ユーザが存在しない)場合は、HSETで追加する。