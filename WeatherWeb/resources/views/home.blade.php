<!doctype html>
<head>
    <script type="text/javascript" src="{{ URL::asset('js/MooTools-Core-1.6.0.js') }}"></script>
    <!--script type="text/javascript" src="{{ URL::asset('js/MooTools-More-1.6.0.js') }}"></script-->
    <!--script type="text/javascript" src="{{ URL::asset('js/mootools-core.js') }}"></script-->
    <script type="text/javascript" src="{{ URL::asset('js/mootools-more.js') }}"></script>
    <script type="text/javascript" src="{{ URL::asset('js/Source/Locale.en-US.DatePicker.js') }}"></script>
    <script type="text/javascript" src="{{ URL::asset('js/Source/Picker.js') }}"></script>
    <script type="text/javascript" src="{{ URL::asset('js/Source/Picker.Attach.js') }}"></script>
    <script type="text/javascript" src="{{ URL::asset('js/Source/Picker.Date.js') }}"></script>
    <script type="text/javascript" src="{{ URL::asset('js/Source/Picker.Date.Range.js') }}"></script>
    <script type="text/javascript" src="{{ URL::asset('js/mui.min.js') }}"></script>
    <!--Highcharts-->
    <script src="http://code.highcharts.com/adapters/mootools-adapter.js"></script>
    <script src="http://code.highcharts.com/5.0.12/highcharts.js"></script>
    <script src="http://code.highcharts.com/modules/exporting.js"></script>
    <!--Styles-->
    <link type="text/css" rel="stylesheet" href="{{ URL::asset('css/style.css') }}">
    <link type="text/css" rel="stylesheet" href="{{ URL::asset('css/Source/datepicker_vista/datepicker_vista.css') }}">
    <link type="text/css" rel="stylesheet" href="{{ URL::asset('css/Source/datepicker.css') }}">
    <link type="text/css" rel="stylesheet" href="{{ URL::asset('css/mui.min.css') }}">

    <script type="text/javascript" src="{{ URL::asset('js/home.js') }}"></script>
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAvqYj6K_pMJOkm4I0xgmdbCxw0x5ycyRI"async defer></script>
</head>
<body>
    <div id="sidedrawer" class="mui--no-user-select">
        </br>
        <label>Select Date:</label>
        <input class="datepicker-input" type="text" id="datepicker" ></br></br>
        <label>Select Station:</label>
        <select class="datepicker-input" id="station-picker">
                <option value="1">Station 1</option>
            </ul>
        </select>
        </br></br>
        <button type="button" id="search-button" class="mui-btn mui-btn--primary" style="display: block">Search</button>
    </div>
    <header id="header">
        <div class="mui-appbar mui--appbar-line-height">
            <div class="mui-container-fluid">
                <a id="help-button" style="color:white;float:right">?</a>
                <a id="show-bar" class="sidedrawer-toggle mui--visible-xs-inline-block mui--visible-sm-inline-block js-show-sidedrawer" style="position:inline-block">▶ Search </a>
                <a id="hide-bar" class="sidedrawer-toggle mui--hidden-xs mui--hidden-sm js-hide-sidedrawer" style="position:inline-block">◀ Search</a>
                <img src="{{ URL::asset('thunder.svg') }}" style="margin:0 auto;position: fixed"><!--SVG icon by AMcharts-->
                <h3 style="margin-left:50px;display:inline-block;">Tramper Weather Monitor</h3>
                <ul class="mui-tabs__bar" style="float:right;display:inline-block;">
                    <li class="mui--is-active" id="nav-table"><a data-mui-toggle="tab" data-mui-controls="main-table" >Table</a></li>
                    <li id="nav-graph"><a data-mui-toggle="tab" data-mui-controls="main-graph" >Graph</a></li>
                    <li id="nav-map"><a data-mui-toggle="tab" data-mui-controls="main-map" >Map</a></li-->
                </ul>
            </div>

        </div>
    </header>
    <div id="content-wrapper">
        <div class="datetime-elements">
            <button type="button" id="prev-date-button"><</button>
            <select id="datetime-picker">
            </select>
            <button type="button" id="next-date-button">></button>
        </div>
        <img id='loader' src="{{ URL::asset('three-dots.svg') }}" style="position:fixed;left:50%;top:50%;"><!--SVG by AMcharts-->
        <div class="mui-tabs__pane mui--is-active" id="main-table" style="display: none" >
            <div class="table-elements">
                <span id="total-main-part"></span>
                <span id="total-main-data"></span>
                <button type="button" id="prev-main-button">Back</button>
                <button type="button" id="next-main-button">Next</button>
            </div>
            <table class="mui-table mui-table--bordered" style="width: 90%;">
                <thead>
                    <tr>
                        <th>Temperature</th>
                        <th>Pressure</th>
    		            <th>Wind Speed</th>
                        <th>Wind Direction</th>
                        <th>Rainfall</th>
    		            <th>Humidity</th>
                        <th><a id="sort-date-button" href="#">Date</a></th>
    	            </tr>
                </thead>
                <tbody id="data-result">
    		    </tbody>
            </table>
        </div>
        <div class="mui-tabs__pane" id="main-graph" style="display: none;">
            <div id="container-one"></div>
        </div>
        <div class="mui-tabs__pane" id="main-map" style="display: none">
            <div id="map"></div>
            <div id="side-map" class="sidedrawer-map">
                <div><!--Radio button designed and created by WILDER TAYPE-->
                <label>
                    <input type="radio" id="t-option" name="units" value="temp" class="option-input radio" checked>
                    Temperature
                </label>
                <label>
                    <input type="radio" id="w-option" name="units" value="wind" class="option-input radio" >
                    Wind Speed/Direction
                </label>
                <label>
                    <input type="radio" id="p-option" name="units" value="pressure" class="option-input radio" >
                    Pressure
                </label>
                <label>
                    <input type="radio" id="h-option" name="units" value="humidity" class="option-input radio" >
                    Humidity
                </label>
                <label>
                    <input type="radio" id="r-option" name="units" value="rainfall" class="option-input radio" >
                    Rainfall
                </label>
            </div>
            </div>
        </div>
    </div>
    <footer id="footer">
        <div class="mui-container-fluid">
        <br>
        Made by Team Weather Monitor - Student Project @ <a href="http://www.weltec.ac.nz/">WelTec</a>
        </div>
    </footer>
</body>
