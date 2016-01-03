<?php
	require_once('init.php');
	require_once('classes/LocationLog.php');
	
	function insert(LocationLog $locationLog) {
		global $database;

		$stmt = $database->prepare("
			INSERT INTO location_log (car_id, latitude, longitude, datetime) VALUES (:car_id,:latitude,:longitude, date('now'));");

		$carId = $locationLog->getCarId();
		$latitude = $locationLog->getLatitude();
		$longitude = $locationLog->getLongitude();

		$stmt->bindParam(':car_id', $carId, PDO::PARAM_INT);
		$stmt->bindParam(':latitude', $latitude, PDO::PARAM_STR);
		$stmt->bindParam(':longitude', $longitude, PDO::PARAM_STR);

		$val = $stmt->execute();
		
		return $val;

	}

	if (!isset($_REQUEST['carId'])) {
		echo 'No car to register';
		die;
	}

	// Create Log
	$locationLog = new LocationLog();
	$locationLog->setCarId($_REQUEST['carId']);
	$locationLog->setLatitude($_REQUEST['latitude']);
	$locationLog->setLongitude($_REQUEST['longitude']);

	// TODO: Implement login

	// Insert log
	$value = insert($locationLog);

	if ($value) {
		echo '{"status": "OK" }';
	} else {
		echo '{"status": "FAIL" }';
	}
?>
