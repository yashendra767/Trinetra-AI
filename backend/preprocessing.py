import math
import pickle
import pandas as pd
import numpy as np
import os


current_dir = os.path.dirname(os.path.abspath(__file__))
data_dir = os.path.join(current_dir, 'model')


with open(os.path.join(data_dir, 'scalar.pkl'), 'rb') as f:
    scaler = pickle.load(f)

with open(os.path.join(data_dir, 'ipc_sections_encoder.pkl'), 'rb') as f:
    ipc_encoder = pickle.load(f)

with open(os.path.join(data_dir, 'area_encoder.pkl'), 'rb') as f:
    area_encoder = pickle.load(f)

with open(os.path.join(data_dir, 'act_category_encoder.pkl'), 'rb') as f:
    act_encoder = pickle.load(f)

with open(os.path.join(data_dir, 'status_encoder.pkl'), 'rb') as f:
    status_encoder = pickle.load(f)

with open(os.path.join(data_dir, 'reporting_station_encoder.pkl'), 'rb') as f:
    station_encoder = pickle.load(f)

def safe_transform(encoder, value, default=-1):
    try:
        return encoder.transform([value])[0]
    except ValueError:
        print(f"[Warning] Unseen label '{value}' in encoder {type(encoder).__name__}")
        return default

def preprocess(raw):
    ts = pd.to_datetime(raw['timestamp'])
    year = ts.year
    month = ts.month
    day = ts.day
    hour = ts.hour
    weekday = ts.weekday()
    is_weekend = int(weekday in [5, 6])

    # Cyclical encoding
    month_sin = math.sin(2 * math.pi * month / 12)
    month_cos = math.cos(2 * math.pi * month / 12)
    hour_sin = math.sin(2 * math.pi * hour / 24)
    hour_cos = math.cos(2 * math.pi * hour / 24)
    day_sin = math.sin(2 * math.pi * day / 31)
    day_cos = math.cos(2 * math.pi * day / 31)
    weekday_sin = math.sin(2 * math.pi * weekday / 7)
    weekday_cos = math.cos(2 * math.pi * weekday / 7)

    # Encode categorical features
    ipc_sections_encoded = safe_transform(ipc_encoder, raw['ipc_sections'])
    area_encoded = safe_transform(area_encoder, raw['area'])
    act_category_encoded = safe_transform(act_encoder, raw['act_category'])
    status_encoded = safe_transform(status_encoder, raw['status'])
    reporting_station_encoded = safe_transform(station_encoder, raw['reporting_station'])


    features = [
        raw['lat'],
        raw['lng'],
        year,
        is_weekend,
        month_sin, month_cos,
        hour_sin, hour_cos,
        weekday_sin, weekday_cos,
        day_sin, day_cos,
        ipc_sections_encoded, act_category_encoded, area_encoded, status_encoded, reporting_station_encoded
    ]


    numeric_indices = [0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11]
    numeric_part = [features[i] for i in numeric_indices]
    scaled_numeric = scaler.transform([numeric_part])[0]


    final_features = list(scaled_numeric) + [features[i] for i in [3, 12, 13, 14, 15, 16]]

    return final_features