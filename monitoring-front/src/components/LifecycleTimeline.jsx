import React, { useEffect, useState } from "react";
import "./LifecycleTimeline.css";

export default function LifecycleTimeline({ processId, onClose }) {

    const [steps, setSteps] = useState([]);

    useEffect(() => {

        if (!processId) return;

        fetch(`http://localhost:8089/api/monitoring/instances/${processId}/lifecycle`)
            .then(res => res.json())
            .then(data => setSteps(data))
            .catch(console.error);

    }, [processId]);

    return (

        <div className="timeline-modal">

            <div className="timeline-content">

                <h3>Cycle de vie - Process {processId}</h3>

                {steps.map((step, index) => (

                    <div key={index} className="timeline-step">

                        <div className="circle"></div>

                        <div className="step-content">

                           <h4>

{step.status === "ACTIVE" ? "🟠" : "✅"} {step.step}

</h4>

<p>Début : {step.start}</p>

{step.status === "ACTIVE" && (

    <p
        style={{
            color:"#e67e22",
            fontWeight:"bold"
        }}
    >
        Étape en cours
    </p>

)}

                        </div>

                    </div>

                ))}

                <button onClick={onClose}>
                    Fermer
                </button>

            </div>

        </div>

    );

}