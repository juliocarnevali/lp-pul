# To add a new cell, type '# %%'
# To add a new markdown cell, type '# %% [markdown]'
# %% [markdown]
# # Imports
import os
os.environ['CUDA_VISIBLE_DEVICES'] = '-1'


# %%
import tensorflow as tf
import numpy as np
from tensorflow.keras.optimizers import Adam
from scipy.spatial.distance import cosine

# %% [markdown]
# # Functions

# %%
def sim_cosine(vec1, vec2): 
  sim = 1 - cosine(vec1, vec2)
  return sim if not np.isnan(sim) else 0

# %% [markdown]
# # Definitions
tf.config.threading.set_inter_op_parallelism_threads(10)

# %% [markdown]
# # DenseAutoencoder Class

# %%
class DenseAutoencoder(object):

    def __init__(self, num_epochs=100, learning_rate=0.01, batch_size=1, layers=[], threshold=0.1): 
        self.num_epochs = num_epochs
        self.learning_rate = learning_rate
        self.batch_size=batch_size
        self.model = None 
        self.layers = layers
        self.__threshold = threshold

    def valid_threshold(self,threshold): 
        return threshold if (threshold >=0) and (threshold <=1) else 0


    def set_threshold(self,threshold):
        self.__threshold = self.valid_threshold(threshold)

    def get_params(self):
        return {
            'batch_size': self.batch_size,
            'num_epochs': self.num_epochs, 
            'layers': self.layers
        }

    def fit(self,X):
        input_size = X.shape[1]
        input = tf.keras.Input(shape=(input_size,))
        for i,layer in enumerate(self.layers):
            encoded =  tf.keras.layers.Dense(layer['num_neurons'], activation=layer['activation'])(input) if i == 0 else tf.keras.layers.Dense(layer['num_neurons'], activation=layer['activation'])(encoded)
        decoded = tf.keras.layers.Dense(input_size, activation='sigmoid')(encoded)
        autoencoder = tf.keras.Model(input, decoded) 
        autoencoder.compile(optimizer=Adam(learning_rate=0.01), loss='binary_crossentropy')
        self.model = autoencoder
        print(self.model.summary())
        result = self.model.fit(X,X, epochs=self.num_epochs, shuffle=True, batch_size=self.batch_size) 
        print('Loss:', result.history['loss'][-1])

    def decision_function(self,X): 
        scores = np.zeros(len(X), dtype=np.float32)
        model_outputs = self.model.predict(X)
        for i, output in enumerate(model_outputs): 
            scores[i] = sim_cosine(X[i], output)
        return scores 

    def predict(self,X): 
        predictions = np.zeros(len(X))
        scores = self.decision_function(X)
        for i,score in enumerate(scores): 
            predictions[i] = 1 if score > self.__threshold else -1
        return predictions


# %%


# %% [markdown]
# # Test area

# %%