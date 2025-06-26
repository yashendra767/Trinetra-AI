import xgboost as xgb
import pickle
import pandas as pd

import numpy as np
from preprocessing import preprocess

# Load models and label encoders once
model_dir = '~/backend/model/'
model_zone = xgb.Booster()
model_zone.load_model(model_dir + 'crime_zone_model.json')

model_type = xgb.Booster()
model_type.load_model(model_dir + 'crime_type_model.json')

with open(model_dir + 'zone_label_encoder.pkl', 'rb') as f:
    le_zone = pickle.load(f)
with open(model_dir + 'type_label_encoder.pkl', 'rb') as f:
    le_type = pickle.load(f)

def predict_crime(raw_data):   # raw data is one d numpy array
    features = preprocess(raw_data)
    # Add column names to match what the model expects
    columns = [
        "lat", "lng", "year", "is_weekend",
        "month_sin", "month_cos", "hour_sin", "hour_cos",
        "weekday_sin", "weekday_cos", "day_sin", "day_cos",
        "ipc_sections_encoded", "act_category_encoded",
        "area_encoded", "status_encoded", "reporting_station_encoded"
    ]
    df_input = pd.DataFrame([features], columns=columns)
    dinput = xgb.DMatrix(df_input)  # converting to 2d data (1,features)

    pred_zone = model_zone.predict(dinput)
    pred_type = model_type.predict(dinput)   # (1,np. of classes)

    print("🔎 Raw zone prediction:", pred_zone)
    print("🔎 Raw type prediction:", pred_type)

    label_zone = le_zone.inverse_transform([np.argmax(pred_zone)])   # get the index of the max value
    label_type = le_type.inverse_transform([np.argmax(pred_type)])  # will be a list

    return label_zone[0], label_type[0]