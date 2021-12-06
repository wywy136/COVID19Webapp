'use strict';
const http = require('http');
var assert = require('assert');
const express= require('express');
const app = express();
const mustache = require('mustache');
const filesystem = require('fs');
const url = require('url');
const port = Number(process.argv[2]);

const hbase = require('hbase')
var hclient = hbase({ host: process.argv[3], port: Number(process.argv[4])})

function counterToNumber(c) {
	return Number(Buffer.from(c).readBigInt64BE());
}
function rowToMap(row) {
	var stats = {}
	row.forEach(function (item) {
		stats[item['column']] = item['$']
	});
	return stats;
}

hclient.table('ywang27_country_state_date').row('USIllinois2021-01-01').get((error, value) => {
	console.info(rowToMap(value))
	console.info(value)
})


// hclient.table('weather_delays_by_route').row('ORDAUS').get((error, value) => {
// 	console.info(rowToMap(value))
// 	console.info(value)
// })


app.use(express.static('public'));
app.get('/covid.html',function (req, res) {
    const country_state_date=req.query['country'] + req.query['state'] + req.query['date'];
	console.log(req.query['country']);
	console.log(req.query['state']);
    console.log(country_state_date);
	hclient.table('ywang27_country_state_date').row(country_state_date).get(function (err, cells) {
		console.log(cells);
		if (cells == null){
			var _template = filesystem.readFileSync("result.mustache").toString();
			var _html = mustache.render(_template,  {
				country : req.query['country'],
				state : req.query['state'],
				date: req.query['date'],
				confirmed: "-",
				active: "-",
				deaths: "-",
				recovered: "-",
				recovering_rate: "-",
				death_rate: "-"
			});
			res.send(_html);
		}
		else {
			const covid_info = rowToMap(cells);
			// console.log(covid_info);

			function calculate_rate(numerator, denominator) {
				var n = Number(numerator);
				var d = Number(denominator);
				if (d === 0)
					return " - ";
				return (n / d).toFixed(4) * 100;
			}

			var template = filesystem.readFileSync("result.mustache").toString();
			var html = mustache.render(template, {
				country: req.query['country'],
				state: req.query['state'],
				date: req.query['date'],
				confirmed: covid_info["data:confirmed"],
				active: covid_info['data:active'],
				deaths: covid_info['data:deaths'],
				recovered: covid_info['data:recovered'],
				recovering_rate: calculate_rate(covid_info['data:recovered'], covid_info["data:confirmed"]),
				death_rate: calculate_rate(covid_info['data:deaths'], covid_info["data:confirmed"])
			});
			res.send(html);
		}
	});
});


var kafka = require('kafka-node');
var Producer = kafka.Producer;
var KeyedMessage = kafka.KeyedMessage;
var kafkaClient = new kafka.KafkaClient({kafkaHost: process.argv[5]});
var kafkaProducer = new Producer(kafkaClient);


app.get('/submit.html',function (req, res) {
	var country = req.query['country'];
	var state = req.query['state'];
	var date = req.query['date'];
	var confirmed = req.query['confirmed'];
	var deaths = req.query['deaths'];
	var recovered = req.query['recovered'];
	var active = req.query['active'];

	var report = {
		country: country,
		state: state,
		date: date,
		confirmed: confirmed,
		deaths: deaths,
		recovered: recovered,
		active: active
	};

	kafkaProducer.send([{ topic: 'ywang27_covid', messages: JSON.stringify(report)}],
		function (err, data) {
			console.log(report);
			res.redirect('covid-submit.html');
		});
});

app.listen(port);
