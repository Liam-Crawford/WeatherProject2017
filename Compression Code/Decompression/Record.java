/* This class stores the weather data after is has been processed
 * because most of these are doubles, floating point inaccuracies can occur
 * but are minimal. 
 */

public class Record {
	private int time;
	private double temperature;
	private double humidity;
	private double pressure;
	private double rainfall;
	private double windSpeed;
	private double windDirection;
	
	public Record() {}

	public Record(double temperature, double humidity, double pressure, double rainfall, 
			double windSpeed, double windDirection, int time) {
		this.time = time;
		this.temperature = temperature;
		this.humidity = humidity;
		this.pressure = pressure;
		this.rainfall = rainfall;
		this.windSpeed = windSpeed;
		this.windDirection = windDirection;
	}
	
	public double getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	public double getHumidity() {
		return humidity;
	}
	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}
	public double getPressure() {
		return pressure;
	}
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}
	public double getRainfall() {
		return rainfall;
	}
	public void setRainfall(double rainfall) {
		this.rainfall = rainfall;
	}
	public double getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
	}
	public double getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(double windDirection) {
		this.windDirection = windDirection;
	}
	
	/* Populates the record in a pre-defined order, used when building the data from unpacker in its current form as 
	 * this relies on the number in which the values are sent in order to identify which values they are.
	 */
	public void populateRecord(double value, int type) {
		switch(type) {
			case 0:
				setTemperature(value);
				break;
			
			case 1:
				setPressure(value);
				break;
			
			case 2:
				setHumidity(value);
				break;
				
			case 3:
				setRainfall(value);
				break;
				
			case 4:
				setWindSpeed(value);
				break;
				
			case 5:
				setWindDirection(value);
				break;
				
			default:
				break;
		}	
	}
	
	/* Accesses the data in the ordered manner as mentioned above, used primarily when generating update records
	 * because these need a reference for thier +/- values to be applied to.
	 */
	public double orderedAccess(int type) {
		switch(type) {
			case 0:
				return temperature;
			case 1:
				return pressure;
			case 2:
				return humidity;
			case 3:
				return rainfall;
			case 4:
				return windSpeed;
			case 5:
				return windDirection;
			default:
				return 0;
		}			
	}
}
