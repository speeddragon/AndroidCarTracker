<?php
	require_once('init.php');

    function getLast($limit = 30) {
    	global $database;

    	$stmt = $database->prepare("
    		SELECT *
    		FROM location_log
    		ORDER BY location_log_id DESC
    		LIMIT :limit");
        $stmt->bindParam(':limit', $limit);
    	$stmt->execute();
        
    	$result = $stmt->fetchAll();
    	
    	return $result;
    }

    $last15log = getLast();

    
    if (!isset($gmapLat) && !isset($gmapLon)) {
        $gmapLat = $last15log[0]['latitude'];
        $gmapLon = $last15log[0]['longitude'];
        $zoomLevel = 17;
    }

    if (!isset($zoomLevel)) {
        $zoomLevel = 7;
    }
    
?>
<html>
	<head>
		<link href='https://fonts.googleapis.com/css?family=Droid+Sans:400:700|Russo+One|Unica+One|Inconsolata|Lato:300,400,900|Oswald:400,300,700' rel='stylesheet' type='text/css'>
		<link rel="stylesheet" type="text/css" href="css/style.css">
    </head>
	<body>
		<div class="head">
		<h1>GPS Tracker Viewer</h1>
		</div>
		<div class="last_positions">
			<div>
				<a href="viewer.php">Google Maps</a>
			</div>

			<?php if (is_array($last15log) && count($last15log) > 0) { ?>
			<div>
				Last updated <span class="updated"><?php echo round((time() - strtotime($last15log[0]['datetime']))/60, 0); ?>m</span> ago!
			</div>
			<?php } ?>

			<h2>Last 30 positions</h2>
			<ul>
				<?php foreach($last15log as $entry) { ?>
				<li><?php echo $entry['datetime'] . ' - ' . $entry['latitude'] . ' ' . $entry['longitude']; ?></li>
				<?php } ?>
			</ul>
		</div>
	</body>
</html>