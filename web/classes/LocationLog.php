<?php
	class LocationLog {
		protected $_locationLogId;
		protected $_carId;
		protected $_latitude;
		protected $_longitude;
		protected $_dateTime;

		public function getCarId() {
			return $this->_carId;
		}

		public function setCarId($carId) {
			$this->_carId = $carId;
		}

		public function getLatitude() {
			return $this->_latitude;
		}

		public function setLatitude($latitude) {
			$this->_latitude = $latitude;
		}

		public function getLongitude() {
			return $this->_longitude;
		}

		public function setLongitude($longitude) {
			$this->_longitude = $longitude;
		}

	}