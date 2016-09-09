import React from 'react';
import ReactDOM from 'react-dom';
import {StyleRoot} from 'radium';
import 'whatwg-fetch';
import LoadingIndicator from 'react-loading-indicator';

const MainBox = React.createClass({
  getInitialState: function() {
    return {
      address: '',
      location: '',
      formatedAddress: '',
      status: 'Please, enter some address',
      loading: false,
    };
  },
  handleChange: function(event) {
    const self = this;
    this.setState({
      address: event.target.value,
      loading: true,
    });
    fetch(`/geocode?address="${this.state.address}"`)
      .then(function(response) {
        console.log('response:', response);
        return response.json()
      }).then(function(json) {
        console.log('json: ', json);
        if (json.hasOwnProperty('status') &&
            json.status.toLowerCase()==='ok') {
          self.setState({
            location: json.results[0].geometry.location.lat + ", " +
                      json.results[0].geometry.location.lng,
            formatedAddress: json.results[0].formatted_address,
            status: json.status,
          });
        } else if (json.hasOwnProperty('status')) {
          self.setState({
            location: '',
            formatedAddress: '',
            status: json.status,
          });
        } else {
          self.setState({
            location: '',
            formatedAddress: '',
            status: 'Error: unknown format',
          });
        }
        self.setState({ loading: false });
      }).catch(function(ex) {
        self.setState({
          location: '',
          formatedAddress: '',
          status: 'Error: Parsing failed',
          loading: false,
        });
      })
  },
  render: function() {
    return (
      <div className="mainBox" style={{ width: '100%', height: '100%'}}>
        <StyleRoot>
          <h1>Trucker Path Cache Client</h1>
          <div>Address:</div>
          <input
            type="text"
            value={this.state.address}
            onChange={this.handleChange}
          />
          <div style={{display: this.state.loading ? 'block' : 'none'}}>
            <LoadingIndicator />
          </div>
          <div>Location (latitude, longitude):</div>
          <div>{this.state.location}</div>
          <div>Formated Address:</div>
          <div>{this.state.formatedAddress}</div>
          <div>Status:</div>
          <div>{this.state.status}</div>
        </StyleRoot>
      </div>
    );
  }
});


ReactDOM.render(
  <MainBox />,
  document.getElementById('content')
);

