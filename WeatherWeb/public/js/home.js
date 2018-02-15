function number_format (number, decimals, dec_point, thousands_sep)
{

  var n = !isFinite(+number) ? 0 : +number,
    prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
    sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
    dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
    s = '',
    toFixedFix = function (n, prec) {
      var k = Math.pow(10, prec);
      return '' + Math.round(n * k) / k;
    };
  s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
  if (s[0].length > 3) {
    s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
  }
  if ((s[1] || '').length < prec) {
    s[1] = s[1] || '';
    s[1] += new Array(prec - s[1].length + 1).join('0');
  }
  return s.join(dec);
}

function isValidDate (date)
{
  var IsoDateRe = new RegExp("^([0-9]{4})-([0-9]{2})-([0-9]{2})$");
  var matches = IsoDateRe.exec(date);
  if (!matches) return false;


  var composedDate = new Date(matches[1], (matches[2] - 1), matches[3]);

  return ((composedDate.getMonth() == (matches[2] - 1)) &&
          (composedDate.getDate() == matches[3]) &&
          (composedDate.getFullYear() == matches[1]));

}

var DefaultView = function()
{
    var self               = this;
    self._request          = null;
    self.requestGetDataURL = '/getData';
    //self.requestGetGraphDataURL_prod = '/getGraphData';
    //self.requestGetDataURL = '/weathermonitor/public/getData';
    //self.requestGetGraphDataURL = '/weathermonitor/public/getGraphData';
    self.sideBar           = 'sidedrawer';
    self.showBarButton     = 'show-bar';
    self.hideBarButton     = 'hide-bar';
    self.datePicker        = 'datepicker';
    //self.datePickerTo    = 'datepicker-to';
    self.stationPicker     = 'station-picker';
    self.searchButton      = 'search-button';

    //Navbar
    self.navTable          = 'nav-table';
    self.navGraph          = 'nav-graph';
    self.navMap            = 'nav-map';

    //Table
    self.viewMain          = 'main-table';
    self.viewTable         = 'data-result';
    self.emptyResultId     = 'empty-result';

    //pagination
    self.prevId            = 'prev-main-button';
    self.nextId            = 'next-main-button';
    self.totalDataId       = 'total-main-data';
    self.totalPartId       = 'total-main-part';
    self.pageLimit         = 1;

    //Date/time
    self.dateTimePicker    = 'datetime-picker';
    self.datePrevButton    = 'prev-date-button';
    self.dateNextButton    = 'next-date-button';
    self.dateTimeVal       =  null;
    self.timeCounter       =  null;
    //self.dateCounter       =  0;

    //Help
    self.helpButton        = 'help-button';

    self.resultData        = [];
    self.totalPage         = 1;
    self.currentPage       = 1;
    self.selectedView      = 'Table';

    //sorting
    self.sortOrder         =  null;
    self.sortDate          = 'sort-date-button';
    self.testPostButton    = 'test-post-button';

    //Graph
    self.viewGraph         = 'main-graph';
    self.graphOne          = 'container-one';
    self.graphTwo          = 'container-two';
    self.graphThree        = 'container-three';
    self.graphCategory     = [];
    self.graphTemp         = [];
    self.graphPressure     = [];
    self.graphWindSpeed    = [];
    self.graphRainfall     = [];
    self.graphHumidity     = [];
    self.chartOne          = "";

    //Map
    self.viewMap           = 'main-map';
    self.mapContainer      = null;
    self.infoWindow        = null;
    self.markers           = null;
    self.icon              = null;
    self.coordinates       = null;
    self.radioMap          = 'input[name=units]';
    self.mapTemp           = null;
    self.mapPressure       = null;
    self.mapWindSpeed      = null;
    self.mapWindDirection  = null;
    self.mapRainfall       = null;
    self.mapHumidity       = null;
    self.mapDate           = null;

    //loader
    self.loader            = 'loader';

    self.init = function()
    {
        self.getData();
    };

    self.getData = function()
    {
        self.getAjaxData(
          self.selectedView,
          self.currentPage,
          self.sortOrder,
          self.dateTimeVal,
          //callbacks
          [
            self.addEvents
          ]
        );
    };

    self.disposeData = function()
    {
        $(self.viewTable).empty();
    };

    self.getAjaxData = function(view, page, sort, datetime, callbacks)
    {
        if (!self._request || !self._request.isRunning())
        {

            if (!page)
              page = 1;

            if (datetime == null) // First page call
            {
                //Check and validate date
                if ($(self.datePicker).get('value') == "" || isValidDate($(self.datePicker).get('value')) == false)
                    $(self.datePicker).set('value',new Date().format('%Y-%m-%d'))

                datefrom = $(self.datePicker).get('value') + ' 00:00:00';
                dateto = $(self.datePicker).get('value') + ' 23:59:59';
                self.dateTimeVal = $(self.datePicker).get('value');
            }
            else if (datetime.length == 10)
            {
                datefrom = datetime + ' 00:00:00';
                dateto = datetime + ' 23:59:59';

            }
            else
            {
                //Format date to YYYY-MM-DD HH:MM:SS
                dateto = datetime;
                d = datetime.substring(0,10);
                datefrom = d + ' 00:00:00';
            }



            arrayData = {
                'dateFrom'          : datefrom,
                'dateTo'            : dateto,
                'page'              : page,
                'sort'              : sort

            };

            //Set View
            if (view == 'Graph')
            {

                self.requestGetDataURL = '/getGraphData'; //'/weathermonitor/public/getGraphData';
                callbacks.push(self.renderGraph);
            }
            else if (view == 'Table')
            {

                self.requestGetDataURL = '/getData'; //'/weathermonitor/public/getData';
                callbacks.push(self.renderData);
                callbacks.push(self.paginationChecker);
            }
            else if (view == 'Map')
            {

                self.requestGetDataURL = '/getGraphData';//'/weathermonitor/public/getGraphData';
                select = 'temp';
                callbacks.push(self.renderMap);

            }
            else {

            }


            //Fetch Data from Database
            self._request = new Request.JSON(
            {
                'url' : self.requestGetDataURL,
                'method' : 'GET',
                'data' : arrayData,
                'onRequest' : function(data)
                {
                   $(self.loader).setStyle('display', 'block');
                   $(self.viewMain).setStyle('display', 'none');
                   $(self.viewGraph).setStyle('display', 'none');
                   $(self.viewMap).setStyle('display' , 'none');

                },
                'onSuccess' : function(data)
                {
                    self.disposeData();
                    $(self.loader).setStyle('display', 'none');

                    if (view == 'Table')
                    {
                        self.currentPage        = data.page;
                        self.totalPage          = data.totalPage;
                        self.pageLimit          = data.limit;
                        $(self.viewGraph).setStyle('display', 'none');
                        $(self.viewMap).setStyle('display' , 'none');
                        $(self.viewMain).setStyle('display' , 'block');
                    }
                    else if (view == 'Graph')
                    {
                        $(self.viewMain).setStyle('display' , 'none');
                        $(self.viewMap).setStyle('display' , 'none');
                        $(self.viewGraph).setStyle('display', 'block');

                    }
                    else if (view == 'Map')
                    {
                        $(self.viewMain).setStyle('display' , 'none');
                        $(self.viewGraph).setStyle('display', 'none');
                        $(self.viewMap).setStyle('display' , 'block');

                    }
                    else {

                    }

                    self.resultData         = data.resultData;

                    if (view == 'Table')
                    {
                        if(data.resultData.length)
                        {
                            $$('#' + self.totalDataId).set('html', ' of ' + data.totalData);
                        }
                        else
                        {
                            $$('#' + self.totalDataId).set('html','');
                        }
                    }

                    if(callbacks)
                    {
                        Array.each(callbacks, function(callback)
                        {
                            callback();
                        });
                    }

                },
                'onError' : function(data)
                {
                    self._request.stop;
                }
                }).send();
            }

    };

    //Page Checking - Table
    self.paginationChecker = function()
    {
        $(self.prevId).setStyle('display', 'inline-block');
        $(self.nextId).setStyle('display', 'inline-blockblock');

        //Check the preview button whether it will be disable or not
        if(self.currentPage == 1)
        {
            $(self.prevId).setStyle('display', 'none');
        }
        else
        {
            $(self.prevId).setStyle('display', 'inline-block');
        }

        //Check the next button whether it will be disable or not
        if(self.currentPage < self.totalPage)
        {
            $(self.nextId).setStyle('display', 'inline-block');
        }
        else
        {
            $(self.nextId).setStyle('display', 'none');
        }

        //Below will be the calcutaion and displaying for the total data results
        var start = (self.pageLimit * self.currentPage) - self.pageLimit + 1;
        var end   = (start + self.resultData.length) - 1;

        if(self.resultData.length)
        {
            $(self.totalPartId).set('html', number_format(start) + '-' + number_format(end));
        }
        else
        {
            $(self.totalPartId).set('html', '');

            $(self.prevId).setStyle('display', 'none');
            $(self.nextId).setStyle('display', 'none');
        }
    };

    //Render Table
    self.renderData = function()
    {

        var d = new Date(self.dateTimeVal).format("%Y-%m-%d");

        $(self.dateTimePicker).empty();
        var newElement =  new Element('option');
        newElement.inject($(self.dateTimePicker));
        newElement.setProperty('value', d);
        newElement.appendText(d);

        var x = 1;

        for(var i =0; i < 24; i++)
        {
            var hour = ((i + 12) % 12 + 1);
            var a = i > 11 ? "pm" : "am";
            var ms = x == 24 ? ':59:59' : ':00:00';
            var hhr = x == 24 ? 23 : x++;
            var newElement =  new Element('option');
            newElement.inject($(self.dateTimePicker));
            newElement.setProperty('id', i);
            newElement.setProperty('value', d + ' ' + hhr + ms);
            if (self.timeCounter == i)
            {
                newElement.setAttribute('selected','selected');
            }
            newElement.appendText( d + ' ' + hour + ':00' + a);
        }

        if(self.resultData.length)
        {
            Array.each(self.resultData, function(val, idx)
            {
                var contentHTML = '<td>' + val['temp'] + '</td>'
                                + '<td>' + val['pressure'] + '</td>'
                                + '<td>' + val['wind_speed'] + '</td>'
                                + '<td>' + val['wind_direction'] + '</td>'
                                + '<td>' + val['rainfall'] + '</td>'
                                + '<td>' + val['humidity'] + '</td>'
                                + '<td>' + val['date_received'] + '</td>'

                contentElem = new Element('<tr />',
                {
                    'id'  : '',
                    'html'  : contentHTML
                });
                contentElem.inject($(self.viewTable), "bottom");
            });
        }
        else
        {
            var emptyHTML = '<td colspan="8"><center>No record found.</center></td>';

            emptyElem = new Element('</tr>',
            {
              'id'  : self.emptyResultId,
              'html'  : emptyHTML
            });

            emptyElem.inject($(self.viewTable), "bottom");
        }
    };

    //Render Graph
    self.renderGraph = function()
    {
        self.graphCategory.empty();
        self.graphTemp.empty();
        self.graphPressure.empty();
        self.graphWindSpeed.empty();
        self.graphRainfall.empty();
        self.graphHumidity.empty();


        var d = new Date(self.dateTimeVal).format("%Y-%m-%d");


        $(self.dateTimePicker).empty();
        var newElement =  new Element('option');
        newElement.inject($(self.dateTimePicker));
        newElement.setProperty('value', d);
        newElement.appendText(d);

        var x = 1;

        for(var i =0; i < 24; i++)
        {
            var hour = ((i + 12) % 12 + 1);
            var a = i > 11 ? "pm" : "am";
            var ms = x == 24 ? ':59:59' : ':00:00';
            var hhr = x == 24 ? 23 : x++;
            var newElement =  new Element('option');
            newElement.inject($(self.dateTimePicker));
            newElement.setProperty('id', i);
            newElement.setProperty('value', d + ' ' + hhr + ms);
            if (self.timeCounter == i)
            {
                newElement.setAttribute('selected','selected');
            }
            newElement.appendText( d + ' ' + hour + ':00' + a);
        }

        Array.each(self.resultData, function(val, idx)
        {
            windData = {
                'y'          : val['wind_speed'],
                'direction'  : val['wind_direction']

            };

            self.graphCategory.push(val['date_received']);
            self.graphTemp.push(val['temp']);
            self.graphPressure.push(val['pressure']);
            self.graphWindSpeed.push(windData);
            self.graphRainfall.push(Number.from(val['rainfall']));
            self.graphHumidity.push(val['humidity']);
        });

        self.chartOne = new Highcharts.Chart({
        chart: {
            renderTo: self.graphOne,
            plotBorderWidth: 1,
            plotBorderColor: "#666"
        },
        title: {
            text: 'Weather Graph'
        },
        xAxis: {
            categories: self.graphCategory,//['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            lineColor: '#666',
            tickLength: 0,
            tickPosition: 'inside',
            tickmarkPlacement: 'on',
            tickColor: '#ccc',
            minPadding: 0,
            maxPadding: 0
        },
        yAxis: {
            title: {
                text: null
            },
            labels: {
                enabled: false
            },
            // min: 0,
            // minRange: 1,
        },
        credits: {
            enabled: false
        },
        tooltip: {
            formatter: function() {
                var s = '<b>' + this.x + '</b>',
                    points = this.points,
                    windDirectionPointer = "",
                    counter = 0;

                Array.each(this.points, function(val, idx)
                {
                    s += '<br/>' + val['series']['name'] + ': ' +
                    val['y'] ;

                    if (val['point']['direction'])
                        windDirectionPointer = points[idx].point.direction;

                });

                if (windDirectionPointer)
                {
                    s += '<br/> Wind Direction : ' +  windDirectionPointer;
                }
                return s;

            },
            shared: true
        },
        series: [{
            name: 'Temperature',
            data: self.graphTemp//[29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
        }, {
            name: 'Pressure',
            data: self.graphPressure//[28.7, 99.5, 106.3, 992, 992,]
        }, {
            name: 'Rainfall',
            data: self.graphRainfall
        }, {
            name: 'Humidity',
            data: self.graphHumidity
        }, {
            name: 'Wind Speed',
            data: self.graphWindSpeed
        }]
        },
        function(chart)
        { // on complete
            if (self.graphTemp.length < 1)
            { // check series is empty
                text = chart.renderer.text("No Data Available").add();
                textBBox = text.getBBox();
                x = chart.plotLeft + (chart.plotWidth  * 0.5) - (textBBox.width  * 0.5);
                y = chart.plotTop  + (chart.plotHeight * 0.5) - (textBBox.height * 0.25);
                text.attr({x: x, y: y}).css({color: '#4572A7',fontSize: '16px'});
            }

        }
        );
    };

    //Render Map
    self.renderMap = function()
    {
        var d = new Date(self.dateTimeVal).format("%Y-%m-%d");

        $(self.dateTimePicker).empty();
        var newElement =  new Element('option');
        newElement.inject($(self.dateTimePicker));
        newElement.setProperty('value', d);
        newElement.appendText(d);

        var x = 1;

        for(var i =0; i < 24; i++)
        {
            var hour = ((i + 12) % 12 + 1);
            var a = i > 11 ? "pm" : "am";
            var ms = x == 24 ? ':59:59' : ':00:00';
            var hhr = x == 24 ? 23 : x++;
            var newElement =  new Element('option');
            newElement.inject($(self.dateTimePicker));
            newElement.setProperty('id', i);
            newElement.setProperty('value', d + ' ' + hhr + ms);
            if (self.timeCounter == i)
            {
                newElement.setAttribute('selected','selected');
            }
            newElement.appendText( d + ' ' + hour + ':00' + a);
        }

        if (self.resultData.length > 0)
        {
            $('side-map').setStyle('display','inline-block');
            $('t-option').set('checked',true);

            if ($(self.stationPicker).get('value') == '1')
            {
                self.coordinates = {lat: -41.223959, lng: 174.884247};
            }
            else
            {
                self.coordinates = {lat: -41.223959, lng: 174.884247};
            }



            self.mapTemp = self.resultData[0]['temp'].toString();
            self.mapPressure = self.resultData[0]['pressure'].toString();
            self.mapWindSpeed = self.resultData[0]['wind_speed'].toString();
            self.mapWindDirection = self.resultData[0]['wind_direction'].toString();
            self.mapRainfall = self.resultData[0]['rainfall'].toString();
            self.mapHumidity = self.resultData[0]['humidity'].toString();
            self.mapDate = self.resultData[0]['date_received'];

            var contentString = '<div id="content">'+
           '<div id="siteNotice">'+
           '</div>'+
           '<h4> Weather Info </h4>'+
           '<div id="bodyContent">'+
           '<p><i> Last read: ' + self.mapDate + '</i> </br>' +
           '<b> Station ' + self.resultData[0]['station_id'] + '</b> </br>' +
           '<b> Lat/Lang (-41.223959, 174.884247) </b> </br> </br>' +
           '<b> Temperature: ' + self.mapTemp + '°C </b> </br>' +
           '<b> Pressure: ' + self.mapPressure + ' hPa </b> </br>' +
           '<b> Rainfall: ' + self.mapRainfall + '%</b> </br>' +
           '<b> Humidity: ' + self.mapHumidity + '%</b> </br>' +
           '<b> Wind Speed: ' + self.mapWindSpeed + ' km/h </b> </br>' +
           '<b> Wind Direction: ' + self.mapWindDirection + '° </b> </br>' +
           '</div>'+
           '</div>';

            self.mapContainer = new google.maps.Map($('map'), {
              zoom: 14,
              mapTypeId: 'terrain',
              center: self.coordinates
            });

            self.infoWindow = new google.maps.InfoWindow({
              content: contentString
            });
            self.icon = {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 20,
                strokeWeight: 6,
                fillColor: 'white',
                fillOpacity: 1.0,
                strokeColor: '#ADD8E6'
            }
            self.markers = new google.maps.Marker({
              position: self.coordinates,
              icon: self.icon,
              label : self.mapTemp + '°C',
            });

            self.markers.setMap(self.mapContainer);

            self.markers.addListener('click', function() {
              self.infoWindow.open(self.mapContainer, self.markers);
            });
        }
        else {
            $('map').empty();
            $('map').setStyle('background-color','#fff');
            $('side-map').setStyle('display','none');

            var contentHTML = 'No Data Available'


            contentElem = new Element('<p />',
            {
                'id'  : '',
                'html'  : contentHTML,
                styles: {
                    color: '#4572A7',
                    fontSize: '16px',
                    'margin-top': '20%'
                }
            });
            contentElem.inject($('map'));
        }
    }

    //Help modal
    self.startHelpModal = function()
    {

        var contentHTML = '<h4> About Us </h4></br>'
                        + '<p class=modal-content>This is a Student Project, done by graduating students at WelTec.'
                        + 'The main purpose of this Web Application prototype is for viewing weather data '
                        + 'gathered by people using our own created android app to retrieve data '
                        + 'from various weather devices we have installed. Currently for the purpose '
                        + 'of this project, we only have one weather station available. </p>'
                        + '<h4> How To Use </h4></br>'
                        + '<p class=modal-content>To use this Web App, you can select the date thru the side bar or on the datetime picker.'
                        + 'You can also select the time from the dropdown in the datetime picker. On the sidebar you have an '
                        + 'You can select what type of view you would have using the three Tab buttons on upper right. '
                        + 'Thats it!</p>'


        var modal = new Element('<div />',
        {
            'id'  : '',
            'html'  : contentHTML
        });

        modal.setStyle('width','80%');
        modal.setStyle('height','80%');
        modal.setStyle('margin','100px auto');
        modal.setStyle('backgroundColor','#fff');

        // show modal
        mui.overlay('on', modal);
    }

    //Add element events
    self.addEvents = function()
    {
        new Picker.Date($(self.datePicker), {
            timePicker: false,
            format: '%Y-%m-%d',
            positionOffset: {x: 5, y: 0},
            pickerClass: 'datepicker_vista',
            useFadeInOut: !Browser.ie,
            maxDate : new Date()
        });

        $$(self.radioMap).removeEvents();
        $$(self.radioMap).addEvent('change',function(e)
        {
            var value = $$('input[name=units]:checked')[0].get('value');
            switch (value)
            {
                case 'temp':
                    self.markers.setMap(null);
                    self.markers = new google.maps.Marker({
                      icon: self.icon,
                      position: self.coordinates,
                      label : self.mapTemp + '°C'
                    });
                    self.markers.setMap(self.mapContainer);
                    self.markers.addListener('click', function() {
                      self.infoWindow.open(self.mapContainer, self.markers);
                    });
                   break;
                case 'wind':
                    self.markers.setMap(null);
                    var x = self.mapWindDirection;
                    var wd = '';
                    if (x >= 345 && x <= 360 || x >= 0 && x <= 14)
                    {
                        wd = 'N'

                    } else if (x >= 15 && x <= 30) {
                        wd = 'NNE'

                    } else if (x >= 31 && x <= 60) {
                        wd = 'NE'

                    } else if (x >= 61 && x <= 79) {
                        wd = 'ENE'

                    } else if (x >= 80 && x <= 104) {
                        wd = 'E'

                    } else if (x >= 105 && x <= 120) {
                        wd = 'ESE'

                    } else if (x >= 121 && x <= 150) {
                        wd = 'SE'

                    } else if (x >= 151 && x <= 164) {
                        wd = 'SSE'

                    } else if (x >= 165 && x <= 194) {
                        wd = 'S'

                    } else if (x >= 195 && x <= 209) {
                        wd = 'SSW'

                    } else if (x >= 210 && x <= 239) {
                        wd = 'SW'

                    } else if (x >= 240 && x <= 254) {
                        wd = 'WSW'

                    } else if (x >= 255 && x <= 284) {
                        wd = 'W'

                    } else if (x >= 285 && x <= 299) {
                        wd = 'WNW'

                    } else if (x >= 300 && x <= 330) {
                        wd = 'NW'

                    } else if (x >= 330 && x <= 344) {
                        wd = 'NNW'

                    }

                    self.markers = new google.maps.Marker({

                      icon: self.icon,
                      position: self.coordinates,
                      label : self.mapWindSpeed + 'km/h ' + wd
                    });
                    self.markers.setMap(self.mapContainer);
                    self.markers.addListener('click', function() {
                      self.infoWindow.open(self.mapContainer, self.markers);
                    });
                   break;
                case 'pressure':

                    self.markers.setMap(null);
                    self.markers = new google.maps.Marker({
                      icon: self.icon,
                      position: self.coordinates,
                      label : self.mapPressure + 'hPa'
                    });
                    self.markers.setMap(self.mapContainer);
                    self.markers.addListener('click', function() {
                      self.infoWindow.open(self.mapContainer, self.markers);
                    });
                   break;
                case 'humidity':

                    self.markers.setMap(null);
                    self.markers = new google.maps.Marker({
                      icon: self.icon,
                      position: self.coordinates,
                      label : self.mapHumidity + '%'
                    });
                    self.markers.setMap(self.mapContainer);
                    self.markers.addListener('click', function() {
                      self.infoWindow.open(self.mapContainer, self.markers);
                    });
                   break;
                case 'rainfall':

                   self.markers.setMap(null);
                   self.markers = new google.maps.Marker({
                     icon: self.icon,
                     position: self.coordinates,
                     label : self.mapRainfall + '%'
                   });
                   self.markers.setMap(self.mapContainer);
                   self.markers.addListener('click', function() {
                     self.infoWindow.open(self.mapContainer, self.markers);
                   });
                   break;
                default:

                   self.markers.setMap(null);
                   self.markers = new google.maps.Marker({
                     icon: self.icon,
                     position: self.coordinates,
                     label : self.mapTemp + '°C'
                   });
                   self.markers.setMap(self.mapContainer);
                   self.markers.addListener('click', function() {
                     self.infoWindow.open(self.mapContainer, self.markers);
                   });
           }
        });

        $(self.helpButton).removeEvents();
        $(self.helpButton).addEvent('click', function(e)
        {
            e.preventDefault();
            self.startHelpModal();
        });

        $(self.datePrevButton).removeEvents();
        $(self.datePrevButton).addEvent('click',function(e)
        {
            e.preventDefault();
            self.timeCounter = null;
            self.dateCounter++;

            var d = new Date(self.dateTimeVal).decrement();
            self.dateTimeVal = d.format("%Y-%m-%d");
            $(self.datePicker).set('value',new Date(self.dateTimeVal).format('%Y-%m-%d'));

            self.getData();
        });


        $(self.dateNextButton).removeEvents();
        $(self.dateNextButton).addEvent('click',function(e)
        {
            e.preventDefault();

            self.timeCounter = null;
            self.dateCounter--;
            var d = new Date(self.dateTimeVal).increment();
            self.dateTimeVal = d.format("%Y-%m-%d");
            $(self.datePicker).set('value',new Date(self.dateTimeVal).format('%Y-%m-%d'));
            self.getData();
        });

        $(self.dateTimePicker).removeEvents();
        $(self.dateTimePicker).addEvent('change',function(e)
        {
            e.preventDefault();
            self.currentPage = 1;
            $(self.datePicker).set('value',new Date(self.dateTimeVal).format('%Y-%m-%d'))
            self.dateTimeVal = this.get('value');
            self.timeCounter = this.getSelected()[0].get('id');
            self.getData();
        });

        $(self.searchButton).removeEvents();
        $(self.searchButton).addEvent('click', function(e)
        {
            e.preventDefault();
            self.dateTimeVal = $(self.datePicker).get('value');
            self.dateCounter = 0;
            self.timeCounter = null;
            self.getData();

        });

        $(self.prevId).removeEvents();
        $(self.prevId).addEvent('click', function(e)
        {
            e.preventDefault();


            self.currentPage--;
            self.getData();

        });

        $(self.nextId).removeEvents();
        $(self.nextId).addEvent('click', function(e)
        {
            e.preventDefault();


            self.currentPage++;
            self.getData();


        });

        $(self.sortDate).removeEvents();
        $(self.sortDate).addEvent('click', function()
        {
            if(self.sortOrder == 'ASC')
            {
                self.sortOrder = 'DESC';
                self.getData();
            }
            else
            {
                self.sortOrder = 'ASC';
                self.getData();
            }

        });

        $(self.navTable).removeEvents();
        $(self.navTable).addEvent('click', function(e)
        {
            e.preventDefault();
            self.selectedView = 'Table';

            self.getData();


        });

        $(self.navGraph).removeEvents();
        $(self.navGraph).addEvent('click', function(e)
        {
            e.preventDefault();
            self.selectedView = 'Graph';

             self.getData();


        });

        $(self.navMap).removeEvents();
        $(self.navMap).addEvent('click', function(e)
        {
            e.preventDefault();
            self.selectedView = 'Map';

            self.getData();


        });

        $(self.showBarButton).removeEvents();
        $(self.showBarButton).addEvent('click', function(e)
        {
            $(self.showBarButton).setStyle('display', 'none');
            $(self.hideBarButton).setStyle('display', 'inline-block');
        });

        $(self.hideBarButton).removeEvents();
        $(self.hideBarButton).addEvent('click', function(e)
        {

            $(document.body).toggleClass('hide-sidedrawer');
            if (document.body.hasClass('hide-sidedrawer'))
            {
                $(self.hideBarButton).set('text', '▶ Search');
                if (self.selectedView == 'Graph')
                {
                    height = $$('div.highcharts-container')[0]['style']['height'];
                    width = $$('div.highcharts-container')[0]['style']['width'];
                    height = height.substr(0,3);
                    width = width.substr(0,4);
                    self.chartOne.setSize(width.toInt() + 200, height.toInt(), doAnimation = true);

                }
            }
            else {

                $(self.hideBarButton).set('text', '◀ Search');
                if (self.selectedView == 'Graph')
                {
                    height = $$('div.highcharts-container')[0]['style']['height'];
                    width = $$('div.highcharts-container')[0]['style']['width'];
                    height = height.substr(0,3);
                    width = width.substr(0,4);
                    self.chartOne.setSize(width.toInt() - 200, height.toInt(), doAnimation = true);
                }
            }


        });

    };
};


var WeatherMonitor =
{

    weatherMonitorObj : null,

    init : function()
    {

        var self = this;
        this.initDefault();
    },

    initDefault : function()
    {
        var self = this;
        self.weatherMonitorObj = new DefaultView();
        self.weatherMonitorObj.init();
    }
};

window.addEvent('domready', function()
{
    WeatherMonitor.init();
});
