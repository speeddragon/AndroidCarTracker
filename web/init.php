<?php
	error_reporting(E_ALL);

	try
	{
		$database = new PDO("sqlite:gpstracker.db");
		$database->setAttribute(PDO::ATTR_ERRMODE, 
                            PDO::ERRMODE_EXCEPTION);

		// Check if tables exists, or else create it

		// Location Log
		$query = $database->query("SELECT name FROM sqlite_master WHERE type='table' AND name='location_log';");
		
		if (!$query->fetch()) {
			$database->exec("
				CREATE TABLE location_log (
					location_log_id INTEGER PRIMARY KEY AUTOINCREMENT,
					car_id INTEGER,
					latitude TEXT,
					longitude TEXT,
					datetime DATETIME
				);");
		}

		// Server Settings
		$query = $database->query("SELECT name FROM sqlite_master WHERE type='table' AND name='com_settings';");
		
		if (!$query->fetch()) {
			$database->exec("
				CREATE TABLE com_settings (
				    set_name TEXT,
				    set_value TEXT,
				    set_info TEXT
				);

				INSERT INTO com_settings (set_name, set_value, set_info) VALUES 
				('SERVER_PING_TIME', 30, 'Number of minutes between each server mandatory call');
				INSERT INTO com_settings (set_name, set_value, set_info) VALUES 
				('ALERT', 'off', 'If alert is set on, gps will comunicate every time with the server');
			");
		}

	}
	catch(PDOException $e)
	{
		die ($e->getMessage());
	}
