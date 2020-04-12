import React from 'react';

function Runner(props){
    if(props.tr.runId) return (
        <div className="Runner">
            {`${props.tr.name}: ${props.tr.status} RUN: ${props.tr.runId}`}
        </div>
    )
    return (
        <div className="Runner">
            {`${props.tr.name}: ${props.tr.status}`}
        </div>
    )
}

export default Runner;