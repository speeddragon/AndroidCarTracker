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
		<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>

		<script>
	        var map;
	        function initialize() {
	            var mapOptions = {
	                zoom: <?php echo is_numeric($zoomLevel) ? $zoomLevel : 0; ?>,
	                center: new google.maps.LatLng(<?php echo is_numeric($gmapLat) ? $gmapLat : 0; ?>, <?php echo is_numeric($gmapLon) ? $gmapLon : 0; ?>),
	                mapTypeId: google.maps.MapTypeId.ROADMAP,
	                streetViewControl: false
	            };

	            if (document.getElementById('map-canvas')) {
	                map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
	                loadMarkers();
	            }
	        }

	        function loadMarkers() {
	        	<?php $i = 0; foreach($last15log as $entry) { ?>
	        	var marker<?php echo $i; ?> = new google.maps.Marker({
				      position: new google.maps.LatLng(<?php echo $entry['latitude']; ?>,<?php echo $entry['longitude']; ?>),
				      map: map,
				      title: '<?php echo $entry["datetime"]; ?>',
				      icon: '<?php echo $i == 0 ? "images/blue.png" : ( $i+1 == count($last15log) ? "images/green.png" : "images/marker.png"); ?>',
				      opacity: <?php echo $i+1  == count($last15log) ? '0' : ((count($last15log) - $i)/count($last15log)); ?>

				  });
	        	<?php $i++; } ?>
	        }

	        google.maps.event.addDomListener(window, 'load', initialize);
	    </script>
	</head>
	<body style="margin: 0px auto;">
		<div id="map-canvas" style="width: 100%; height: 100%;"></div>
	</body>
</html>
