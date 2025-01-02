#!/bin/zsh
# Mac環境用のDocker起動シェル
# 以下3つの変数を設定する。
DOCKER_COMPOSE_PATH="docker-compose-local.yml"
CONTAINER_NAME="pokego-data-api"
ROOT="/Users/sakumar/Project/pokego-data-api"

cd $ROOT
echo "ベースディレクトリ："$(pwd)

STATUS=$(docker-compose ls | \
	grep $CONTAINER_NAME | \
	sed -r "s/^.*(created|restarting|running|removing|paused|exited|dead).*$/\1/"
)

STATUS_DISP=$STATUS
if [ -z $STATUS ]; then
	STATUS_DISP="未作成or停止中"
fi

echo "現在のdocker-composeのステータス：$STATUS_DISP"

case $STATUS in
	removing|dead)
		echo "起動できないステータスです。"
		return
		;;
	running)
		echo "既に起動されています。逆に停止しますか？[y/n]"
		read YN
		
		case $YN in
			[Yy])
				docker-compose -f ./$DOCKER_COMPOSE_PATH stop
				
				echo "実行後の状態："
				echo $(docker-compose -f ./$DOCKER_COMPOSE_PATH ps)
				;;
			*)
				echo "何も実行しませんでした。"
				;;
		esac
		return
		;;
	*)
		;;
esac

echo -n "Dockerコンテナを起動します。[y/n]"
read YN

case $YN in
	[Yy])
		case $STATUS in
			"")
				# コンテナが定義されていない場合
				docker-compose -f ./$DOCKER_COMPOSE_PATH up -d
				;;
			created|exited)
				docker-compose -f ./$DOCKER_COMPOSE_PATH start
				;;
			restarting)
				docker-compose -f ./$DOCKER_COMPOSE_PATH restart
				;;
			paused)
				docker-compose -f ./$DOCKER_COMPOSE_PATH unpause
				;;
			*)
				echo "あり得ないステータス。STATUS: $STATUS"
				return
				;;
		esac
		echo "実行後の状態："
		echo $(docker-compose ls | grep $CONTAINER_NAME)
		;;
	*)
		echo "起動されませんでした。"
		;;
esac
