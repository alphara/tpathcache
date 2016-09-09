import React from 'react';
import ReactDOM from 'react-dom';
import {StyleRoot} from 'radium';
import 'whatwg-fetch';
import LoadingIndicator from 'react-loading-indicator';

const REQUEST_PERIOD = 300;


const MainBox = React.createClass({

  getInitialState: function () {
    return {
      address: '',
      location: '',
      formatedAddress: '',
      status: 'Please, enter some address',
      loading: false,
      lastRequestAt: 0,
    };
  },

  fetchGeocode: function () {
    const self = this;
    this.setState({
      loading: true,
      lastRequestAt: (new Date()).getTime(),
    });
    fetch(`/geocode?address="${this.state.address}"`)
      .then((response) => {
        console.log('response:', response);
        return response.json()
      }).then((json) => {
        console.log('json: ', json);
        if (json.hasOwnProperty('status') &&
            json.status.toLowerCase()==='ok') {
          self.setState({
            location: json.results[0].geometry.location.lat + ", " +
                      json.results[0].geometry.location.lng,
            formatedAddress: json.results[0].formatted_address,
            status: json.status,
          });
        } else {
          self.setState({
            location: '',
            formatedAddress: '',
            status: json.hasOwnProperty('status') ? json.status :
                    'Error: unknown format',
          });
        }
        self.setState({ loading: false });
      }).catch((ex) => {
        self.setState({
          location: '',
          formatedAddress: '',
          status: 'Error: Parsing failed',
          loading: false,
        });
      });
  },

  handleChange: function (event) {
    this.setState({ address: event.target.value });
    if (this.nextRequestTimeout) {
      clearTimeout(this.nextRequestTimeout);
    }
    let msTillNextRequest = (this.state.lastRequestAt + REQUEST_PERIOD) -
                                                       (new Date()).getTime();
    if (msTillNextRequest > 0) {
      this.nextRequestTimeout = setTimeout(this.fetchGeocode,
                                           msTillNextRequest);
    } else {
      this.fetchGeocode();
    }
  },

  mainDivStyle: {
    width: '100%',
    height: '100%',
    color: 'dimgray',
  },

  boxStyle: {
    width: '50%',
    height: '80%',
    margin: '10% auto',
    border: '1px solid darkgray',
    padding: '20px',
  },

  h1Style: {
    textAlign: 'center'
  },

  loadingBoxStyle: {
            width: '20px',
            height: '20px',
            textAlign: 'center',
            margin: '0 auto',
  },

  labelStyle: {
    fontFamily: 'Arial, "Helvetica Neue", Helvetica, sans-serif',
    fontSize: '14px',
    padding: '12px 0px',
    margin: '8px 5px',
  },

  inputStyle: {
    fontFamily: 'Arial, "Helvetica Neue", Helvetica, sans-serif',
    fontSize: '16px',
    width: '100%',
    padding: '12px 20px',
    margin: '8px 5px',
    boxSizing: 'border-box',
    border: 'none',
    borderBottom: '2px solid gray',
    color: 'black'
  },

  render: function() {
    return (
      <div style={this.mainDivStyle}>
      <div style={this.boxStyle}>
        <StyleRoot>
          <h1 style={this.h1Style}>Trucker Path Cache Client</h1>
          <div style={this.loadingBoxStyle}>
            <div style={{
              display: this.state.loading ? 'block' : 'none',
              textAlign: 'center'
            }}>
              <LoadingIndicator />
            </div>
          </div>
          <div style={this.labelStyle}>Address:</div>
          <input
            style={this.inputStyle}
            type="text"
            value={this.state.address}
            onChange={this.handleChange}
          />
          <div style={this.labelStyle}>Location (latitude, longitude):</div>
          <div style={this.inputStyle}>{this.state.location}</div>
          <div style={this.labelStyle}>Formated Address:</div>
          <div style={this.inputStyle}>{this.state.formatedAddress}</div>
          <div style={this.labelStyle}>Status:</div>
          <div style={this.inputStyle}>{this.state.status}</div>
        </StyleRoot>
      </div>
      </div>
    );
  }

});


ReactDOM.render(
  <MainBox />,
  document.getElementById('content')
);

