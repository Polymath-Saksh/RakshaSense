import streamlit as st
import time

sensor_data = {'x': 0.0, 'y': 0.0, 'z': 0.0, 'accX': 0.0, 'accY': 0.0, 'accZ': 0.0}

def update_data():
    # Simulate receiving data from your Android app
    sensor_data['x'] += 0.1  # Replace with actual data from your app
    sensor_data['y'] += 0.2
    sensor_data['z'] += 0.3
    sensor_data['accX'] += 0.4
    sensor_data['accY'] += 0.5
    sensor_data['accZ'] += 0.6

def main():
    st.title("Sensor Data")
    update_data()  # Call the update_data function to update sensor_data
    st.write("Gyroscope:")
    st.write(f"X: {sensor_data['x']:.2f}")
    st.write(f"Y: {sensor_data['y']:.2f}")
    st.write(f"Z: {sensor_data['z']:.2f}")
    st.write("Accelerometer:")
    st.write(f"accX: {sensor_data['accX']:.2f}")
    st.write(f"accY: {sensor_data['accY']:.2f}")
    st.write(f"accZ: {sensor_data['accZ']:.2f}")

if __name__ == "__main__":
    main()