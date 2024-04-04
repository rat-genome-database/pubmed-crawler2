. /etc/profile
MYJAVA="java -Xms512m -Xmx20480m"
APP_DIR=/rgd/pubmed
DATA_DIR=$APP_DIR/pubmed-output

#rm $DATA_DIR/*
aws s3 cp s3://emr-repository/output/ $DATA_DIR/ --recursive

cd $APP_DIR/PubmedCrawlerPipeline
$MYJAVA -jar lib/PubmedCrawlerPipeline.jar --indexer --o $DATA_DIR

aws s3 rm s3://emr-repository/pubmed/ --recursive
aws s3 rm s3://emr-repository/output/ --recursive

