import { axiosInstance } from "../Axios";

export const getReport = (parameters) => (dispatch) => {

    function base64ToArrayBuffer(base64) {
        const binaryString = window.atob(base64);
        const bytes = new Uint8Array(binaryString.length);
        return bytes.map((byte, i) => binaryString.charCodeAt(i));
    }

    function createAndDownloadBlobFile(body, filename) {
        const blob = new Blob([body]);
        if (navigator.msSaveBlob) {
            navigator.msSaveBlob(blob, filename);
        } else {
            const link = document.createElement('a');
            if (link.download !== undefined) {
                const url = URL.createObjectURL(blob);
                link.setAttribute('href', url);
                link.setAttribute('download', filename);
                link.style.visibility = 'hidden';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } else {
                console.log("Unsupported browser for download operation :( ");
            }
        }
    }

    axiosInstance
        .get("/reports?year=" + parameters.year + "&month=" + parameters.month)
        .then(function (response) {
            const arrayBuffer = base64ToArrayBuffer(response.data);
            createAndDownloadBlobFile(arrayBuffer, "Lunatech-chef " + parameters.month + "-" + parameters.year + " report.xlsx");
        })
        .catch(function (error) {
            console.log("Failed loading Schedules: " + error);
        });

};