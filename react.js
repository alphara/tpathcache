import React from 'react';
import ReactDOM from 'react-dom';
import {StyleRoot} from 'radium';

const MainBox = React.createClass({
  render: function() {
    return (
      <div className="mainBox" style={{ width: '100%', height: '100%'}}>
        <StyleRoot>
          <h1>TP test</h1>
        </StyleRoot>
      </div>
    );
  }
});


ReactDOM.render(
  <MainBox />,
  document.getElementById('content')
);

