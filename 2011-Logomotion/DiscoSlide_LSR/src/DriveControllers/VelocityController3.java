/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DriveControllers;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;

/**
 * Updated VelocityController that uses the new AbstractPID class
 * @author JAG
 */
public class VelocityController3 extends AbstractPID {

    private boolean m_invertedOutput = false;
    private int m_debugCounter = 0;
    private boolean m_debug = false;

    public VelocityController3(double Kp, double Ki, double Kd,
            PIDSource source, PIDOutput output,
            double period, boolean invert) {
        super(Kp, Ki, Kd, source, output, period);
        m_invertedOutput = invert;
    }

    /**
     * Read the input, calculate the output accordingly, and write to the output.
     * This should only be called by the PIDTask
     * and is created during initialization.
     */
    protected void calculate() {
        boolean enabled;
        PIDSource pidInput;

        synchronized (this) {
            if (m_pidInput == null) {
                return;
            }
            if (m_pidOutput == null) {
                return;
            }
            enabled = m_enabled; // take snapshot of these values...
            pidInput = m_pidInput;
        }

        if (enabled) {
            double input = pidInput.pidGet();
            double result;
            PIDOutput pidOutput = null;

            synchronized (this) {
                m_error = m_setpoint - input;

                //Prevent Integral Windup if I term will already set to max output
                if (((m_totalError + m_error) * m_I < m_maximumOutput)
                        && ((m_totalError + m_error) * m_I > m_minimumOutput)) {
                    m_totalError += m_error;
                }
                m_result += (m_P * m_error + m_I * m_totalError + m_D * (m_error - m_prevError));
                m_prevError = m_error;

                if (m_result > m_maximumOutput) {
                    m_result = m_maximumOutput;
                } else if (m_result < m_minimumOutput) {
                    m_result = m_minimumOutput;
                }

                pidOutput = m_pidOutput;
                if (m_invertedOutput) {
                    result = m_result * -1;
                } else {
                    result = m_result;
                }
            }
            debug();
            pidOutput.pidWrite(result);

        }
    }

    /**
     * Inverts the output from the setPoint
     */
    public void setInverted(boolean invert) {
        m_invertedOutput = invert;
    }

    public void setDebug(boolean debug) {
        m_debug = debug;
    }

    public void debug() {
        if (m_debug) {
            if (m_debugCounter > 20) {
                Utils.DiscoUtils.debugPrintln("m_setPoint = " + m_setpoint + " / m_error = " + m_error + " / m_totalError = " + m_totalError + " / m_result = " + m_result);
                m_debugCounter = 0;
            } else {
                m_debugCounter++;
            }
        }
    }
}
