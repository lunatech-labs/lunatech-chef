import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSpinner } from "@fortawesome/free-solid-svg-icons";

const Loading = () => {
  return (
    <div className="col-12">
      <span className="text-primary">
        <FontAwesomeIcon icon={faSpinner} spin size="5x" />
      </span>
      <p>Loading . . .</p>
    </div>
  );
};

export default Loading;
