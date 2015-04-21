<?php
	require('init.php');

	function getSettings() {
		global $database;

    	$query = $database->prepare("
    		SELECT *
    		FROM com_settings");
    	$query->execute();
    	$result = $query->fetchAll();
    	
    	// Handle Data
    	$settings = array();
    	foreach($result as $item) {
    		$settings[$item['set_name']] = $item['set_value'];
    	}

    	return $settings;
	}

	// JSON Settings
	$settings = getSettings();

	echo json_encode($settings);
?>
