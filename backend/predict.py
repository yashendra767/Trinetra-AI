import xgboost as xgb
import pickle
import pandas as pd
import numpy as np
import os
from preprocessing import preprocess


current_dir = os.path.dirname(os.path.abspath(__file__))
model_dir = os.path.join(current_dir, 'model')

# Load models and label encoders once
model_zone = xgb.Booster()
model_zone.load_model(os.path.join(model_dir, 'crime_zone_model.json'))

model_type = xgb.Booster()
model_type.load_model(os.path.join(model_dir, 'crime_type_model.json'))

with open(os.path.join(model_dir, 'zone_label_encoder.pkl'), 'rb') as f:
    le_zone = pickle.load(f)

with open(os.path.join(model_dir, 'type_label_encoder.pkl'), 'rb') as f:
    le_type = pickle.load(f)

def predict_crime(raw_data):
    features = preprocess(raw_data)

    columns = [
        "lat", "lng", "year", "is_weekend",
        "month_sin", "month_cos", "hour_sin", "hour_cos",
        "weekday_sin", "weekday_cos", "day_sin", "day_cos",
        "ipc_sections_encoded", "act_category_encoded",
        "area_encoded", "status_encoded", "reporting_station_encoded"
    ]
    df_input = pd.DataFrame([features], columns=columns)
    dinput = xgb.DMatrix(df_input)

    pred_zone = model_zone.predict(dinput)
    pred_type = model_type.predict(dinput)

    print("🔎 Raw zone prediction:", pred_zone)
    print("🔎 Raw type prediction:", pred_type)

    label_zone = le_zone.inverse_transform([np.argmax(pred_zone)])
    label_type = le_type.inverse_transform([np.argmax(pred_type)])

    return label_zone[0], label_type[0]