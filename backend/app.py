from flask import Flask, request, jsonify
from utils.predict import predict_crime

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    zone, crime_type = predict_crime(data)
    return jsonify({'zone': zone, 'type': crime_type})

if __name__ == '__main__':
    app.run(debug=True)