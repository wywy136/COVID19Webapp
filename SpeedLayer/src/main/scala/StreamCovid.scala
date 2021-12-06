import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Increment
import org.apache.hadoop.hbase.util.Bytes

object StreamCovid {
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  val hbaseConf: Configuration = HBaseConfiguration.create()
  hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
  hbaseConf.set("hbase.zookeeper.quorum", "localhost")
  
  val hbaseConnection = ConnectionFactory.createConnection(hbaseConf)
  val covid_data = hbaseConnection.getTable(TableName.valueOf("ywang27_country_state_date"))

  def addToHbase(kfr: KafkaCovidRecord): String = {
    println("Calling addToHbase")
    // println(kfr.state)
    val rowkey = kfr.country + kfr.state + kfr.date
    println(rowkey)
    val put = new Put(Bytes.toBytes(rowkey))
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("confirmed"), Bytes.toBytes(kfr.confirmed))
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("active"), Bytes.toBytes(kfr.active))
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("deaths"), Bytes.toBytes(kfr.deaths))
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("recovered"), Bytes.toBytes(kfr.recovered))
    covid_data.put(put)
    return "Updated speed layer"
  }
  
  def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println(s"""
        |Usage: StreamFlights <brokers> 
        |  <brokers> is a list of one or more Kafka brokers
        | 
        """.stripMargin)
      System.exit(1)
    }
    
    val Array(brokers) = args

    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("StreamCovid")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // Create direct kafka stream with brokers and topics
    val topicsSet = Set("ywang27_covid")
    // Create direct kafka stream with brokers and topics
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc, PreferConsistent,
      Subscribe[String, String](topicsSet, kafkaParams)
    )

    val serializedRecords = stream.map(_.value)

    val kfrs = serializedRecords.map(rec => mapper.readValue(rec, classOf[KafkaCovidRecord]))

    kfrs.print()
    val processed_covid = kfrs.map(addToHbase)
    processed_covid.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
