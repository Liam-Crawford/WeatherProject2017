<?php

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

// Route::get('/', function () {
    //redirect()->action('HomeController@index');

    // $name = DB::connection()->getDatabaseName();
    // print "Connected to " . $name;
    //
    // $results = DB::select("SELECT * FROM weather_monitor");
    // var_dump($results);
// });
Route::resource('/','HomeController');
Route::get('getData', 'HomeController@getData');
Route::get('getGraphData', 'HomeController@getGraphData')->middleware('ajax');
Route::post('getDataAndroid', 'HomeController@getDataAndroid');
Route::post('postDataAndroid', 'HomeController@postDataAndroid');
