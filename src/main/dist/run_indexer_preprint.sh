. /etc/profile
MYJAVA="java -Xms512m -Xmx20480m"
APP_DIR=/rgd/pubmed
DATA_DIR=$APP_DIR/preprint-output

#rm $DATA_DIR/*
aws s3 cp s3://emr-repository/preprint-output/ $DATA_DIR/ --recursive

cd $APP_DIR/PubmedCrawlerPipeline
$MYJAVA -jar lib/PubmedCrawlerPipeline.jar --indexer --preprint --o $DATA_DIR

aws s3 rm s3://emr-repository/preprint/ --recursive
#aws s3 rm s3://emr-repository/preprint-output/ --recursive