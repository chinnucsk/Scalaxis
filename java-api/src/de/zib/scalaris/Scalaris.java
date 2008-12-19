/**
 *  Copyright 2007-2008 Konrad-Zuse-Zentrum für Informationstechnik Berlin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.zib.scalaris;

import java.io.IOException;
import java.util.ArrayList;

import com.ericsson.otp.erlang.OtpAuthException;
import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

/**
 * Provides methods to read and write key/value pairs to a scalaris ring.
 * 
 * <p>
 * Each operation is a single transaction. If you are looking for more
 * transactions, use the {@link Transaction} class instead.
 * </p>
 * 
 * <p>
 * Instances of this class can be generated using a given connection to a
 * scalaris node using {@link #Scalaris(OtpConnection)} or without a
 * connection ({@link #Scalaris()}) in which case a new connection is
 * created using {@link ConnectionFactory#createConnection()}.
 * </p>
 * 
 * <p>
 * There are two paradigms for reading and writing values:
 * <ul>
 *  <li> using Java {@link String}s: {@link #read(String)}, {@link #write(String, String)}
 *       <p>This is the safe way of accessing scalaris where type conversions
 *       are handled by the API and the user doesn't have to worry about anything else.</p>
 *       <p>Be aware though that this is not the most efficient way of handling strings!</p>
 *  <li> using custom {@link OtpErlangObject}s: {@link #readObject(OtpErlangString)},
 *       {@link #writeObject(OtpErlangString, OtpErlangObject)}
 *       <p>Here the user can specify custom behaviour and increase performance.
 *       Handling the stored types correctly is at the user's hand.</p>
 *       <p>An example using erlang objects to improve performance for inserting strings is
 *       provided by {@link de.zib.scalaris.examples.CustomOtpFastStringObject} and can be
 *       tested by {@link de.zib.scalaris.examples.FastStringBenchmark}.</p>
 * </ul> 
 * </p>
 * 
 * <h3>Reading values</h3>
 * <code style="white-space:pre;">
 *   String key;
 *   OtpErlangString otpKey;
 *   
 *   Scalaris sc = new Scalaris();
 *   String value             = sc.read(key);          // {@link #read(String)}
 *   OtpErlangObject optValue = sc.readObject(otpKey); // {@link #readObject(OtpErlangString)}
 * </code>
 * 
 * <p>For the full example, see {@link de.zib.scalaris.examples.ScalarisReadExample}</p>
 * 
 * <h3>Writing values</h3>
 * <code style="white-space:pre;">
 *   String key;
 *   String value;
 *   OtpErlangString otpKey;
 *   OtpErlangString otpValue;
 *   
 *   Scalaris sc = new Scalaris();
 *   sc.write(key, value);             // {@link #write(String, String)}
 *   sc.writeObject(otpKey, otpValue); // {@link #writeObject(OtpErlangString, OtpErlangObject)}
 * </code>
 * 
 * <p>For the full example, see {@link de.zib.scalaris.examples.ScalarisWriteExample}</p>
 * 
 * <h3>Publishing topics</h3>
 * <code style="white-space:pre;">
 *   String topic;
 *   String content;
 *   OtpErlangString otpTopic;
 *   OtpErlangString otpContent;
 *   
 *   Scalaris sc = new Scalaris();
 *   sc.publish(topic, content);       // {@link #publish(String, String)}
 *   sc.publish(otpTopic, otpContent); // {@link #publish(OtpErlangString, OtpErlangString)}
 * </code>
 * 
 * <p>For the full example, see {@link de.zib.scalaris.examples.ScalarisPublishExample}</p>
 * 
 * <h3>Subscribing to topics</h3>
 * <code style="white-space:pre;">
 *   String topic;
 *   String URL;
 *   OtpErlangString otpTopic;
 *   OtpErlangString otpURL;
 *   
 *   Scalaris sc = new Scalaris();
 *   sc.subscribe(topic, URL);       // {@link #subscribe(String, String)}
 *   sc.subscribe(otpTopic, otpURL); // {@link #subscribe(OtpErlangString, OtpErlangString)}
 * </code>
 * 
 * <p>For the full example, see {@link de.zib.scalaris.examples.ScalarisSubscribeExample}</p>
 * 
 * <h3>Unsubscribing from topics</h3>
 * 
 * Unsubscribing from topics works like subscribing to topics with the exception
 * of a {@link NotFoundException} being thrown if either the topic does not
 * exist or the URL is not subscribed to the topic.
 * 
 * <code style="white-space:pre;">
 *   String topic;
 *   String URL;
 *   OtpErlangString otpTopic;
 *   OtpErlangString otpURL;
 *   
 *   Scalaris sc = new Scalaris();
 *   sc.unsubscribe(topic, URL);       // {@link #unsubscribe(String, String)}
 *   sc.unsubscribe(otpTopic, otpURL); // {@link #unsubscribe(OtpErlangString, OtpErlangString)}
 * </code>
 * 
 * <h3>Getting a list of subscribers to a topic</h3>
 * <code style="white-space:pre;">
 *   String topic;
 *   OtpErlangString otpTopic;
 *   
 *   Vector&lt;String&gt; subscribers;
 *   OtpErlangList otpSubscribers;
 *   
 *   // non-static:
 *   Scalaris sc = new Scalaris();
 *   subscribers = sc.getSubscribers(topic);             // {@link #getSubscribers(String)}
 *   otpSubscribers = sc.singleGetSubscribers(otpTopic); // {@link #getSubscribers(OtpErlangString)}
 * </code>
 * 
 * <p>For the full example, see {@link de.zib.scalaris.examples.ScalarisGetSubscribersExample}</p>
 * 
 * @author Nico Kruber, kruber@zib.de
 * @version 2.0
 * @since 2.0
 */
public class Scalaris {
	/**
	 * the connection to a chorsharp node
	 */
	private OtpConnection connection;
	
	/**
	 * Constructor, uses the default connection returned by
	 * {@link ConnectionFactory#createConnection()}.
	 * 
	 * @throws ConnectionException
	 *             if the connection fails
	 */
	public Scalaris() throws ConnectionException {
		connection = ConnectionFactory.getInstance().createConnection();
	}

	/**
	 * Constructor, uses the given connection to an erlang node.
	 * 
	 * @param conn
	 *            connection to use for the transaction
	 * 
	 * @throws ConnectionException
	 *             if the connection fails
	 */
	public Scalaris(OtpConnection conn) throws ConnectionException {
		connection = conn;
	}
	
	// /////////////////////////////
	// read methods
	// /////////////////////////////
	
	/**
	 * Gets the value stored under the given {@code key}.
	 * 
	 * @param key
	 *            the key to look up
	 * 
	 * @return the value stored under the given {@code key}
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to fetch the value
	 * @throws NotFoundException
	 *             if the requested key does not exist
	 * @throws UnknownException
	 *             if any other error occurs
	 */
	public OtpErlangObject readObject(OtpErlangString key)
			throws ConnectionException, TimeoutException, UnknownException,
			NotFoundException {
		try {
			connection.sendRPC("transstore.transaction_api", "quorum_read",
					new OtpErlangList(key));
			OtpErlangTuple received = (OtpErlangTuple) connection.receiveRPC();

			/*
			 * possible return values:
			 *  - {Value, Version}
			 *  - {fail, fail}
			 *  - {fail, not_found}
			 *  - {fail, timeout}
			 */
			if (received.elementAt(0).equals(new OtpErlangAtom("fail"))) {
				OtpErlangObject reason = received.elementAt(1);
				if (reason.equals(new OtpErlangAtom("timeout"))) {
					throw new TimeoutException();
				} else if (reason.equals(new OtpErlangAtom("not_found"))) {
					throw new NotFoundException();
				} else {
					throw new UnknownException(
							"Unknow error - erlang error message: "
									+ reason.toString());
				}
			} else {
				// return the value only, not the version:
				OtpErlangObject value = received.elementAt(0);
				return value;
			}
		} catch (OtpErlangExit e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (OtpAuthException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}

	/**
	 * Gets the value stored under the given {@code key}.
	 * 
	 * @param key
	 *            the key to look up
	 * 
	 * @return the (string) value stored under the given {@code key}
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to fetch the value
	 * @throws NotFoundException
	 *             if the requested key does not exist
	 * @throws UnknownException
	 *             if any other error occurs
	 * 
	 * @see #readObject(OtpErlangString)
	 */
	public String read(String key) throws ConnectionException,
			TimeoutException, UnknownException, NotFoundException {
		try {
			CustomOtpStringObject result = new CustomOtpStringObject();
			readCustom(key, result);
			return result.getValue();
//			return ((OtpErlangString) readObject(new OtpErlangString(key)))
//					.stringValue();
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}
	
	/**
	 * Gets the value stored under the given {@code key}.
	 * 
	 * @param key
	 *            the key to look up
	 * @param value 
	 *            container that stores the value returned by scalaris
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to fetch the value
	 * @throws NotFoundException
	 *             if the requested key does not exist
	 * @throws UnknownException
	 *             if any other error occurs
	 * 
	 * @see #readObject(OtpErlangString)
	 * @since 2.1
	 */
	public void readCustom(String key, CustomOtpObject<?> value)
			throws ConnectionException, TimeoutException, UnknownException,
			NotFoundException {
		try {
			value.setOtpValue(readObject(new OtpErlangString(key)));
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}

	// /////////////////////////////
	// write methods
	// /////////////////////////////

	/**
	 * Stores the given {@code key}/{@code value} pair.
	 * 
	 * @param key
	 *            the key to store the value for
	 * @param value
	 *            the value to store
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws UnknownException
	 *             if any other error occurs
	 */
	public void writeObject(OtpErlangString key, OtpErlangObject value)
			throws ConnectionException, TimeoutException, UnknownException {
		try {
			connection.sendRPC("transstore.transaction_api", "single_write",
					new OtpErlangList(new OtpErlangObject[] { key, value }));
			OtpErlangObject received = connection.receiveRPC();
			
			/*
			 * possible return values:
			 *  - commit
			 *  - userabort
			 *  - {fail, not_found}
			 *  - {fail, timeout}
			 *  - {fail, fail}
			 *  - {fail, abort}
			 */
			if (received.equals(new OtpErlangAtom("commit"))) {
				return;
			} else if (received.equals(new OtpErlangAtom("userabort"))) {
				throw new UnknownException("userabort");
			} else {
				// {fail, Reason}
				OtpErlangTuple returnValue = (OtpErlangTuple) received;

				if (returnValue.elementAt(1).equals(
						new OtpErlangAtom("timeout"))) {
					throw new TimeoutException();
				} else {
					throw new UnknownException(
							"Unknow error - erlang error message: "
									+ returnValue.toString());
				}
			}
		} catch (OtpErlangExit e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (OtpAuthException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}

	/**
	 * Stores the given {@code key}/{@code value} pair.
	 * 
	 * @param key
	 *            the key to store the value for
	 * @param value
	 *            the value to store
	 *
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws UnknownException
	 *             if any other error occurs
	 *
	 * @see #writeObject(OtpErlangString, OtpErlangObject)
	 */
	public void write(String key, String value) throws ConnectionException,
			TimeoutException, UnknownException {
		writeCustom(key, new CustomOtpStringObject(value));
//		writeObject(new OtpErlangString(key), new OtpErlangString(value));
	}
	
	/**
	 * Stores the given {@code key}/{@code value} pair.
	 * 
	 * @param key
	 *            the key to store the value for
	 * @param value
	 *            the value to store
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws UnknownException
	 *             if any other error occurs
	 * 
	 * @see #writeObject(OtpErlangString, OtpErlangObject)
	 * @since 2.1
	 */
	public void writeCustom(String key, CustomOtpObject<?> value)
			throws ConnectionException, TimeoutException, UnknownException {
		writeObject(new OtpErlangString(key), value.getOtpValue());
	}

	// /////////////////////////////
	// publish methods
	// /////////////////////////////
	
	/**
	 * Publishes an event under a given {@code topic}.
	 * 
	 * @param topic
	 *            the topic to publish the content under
	 * @param content
	 *            the content to publish
	 *
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 */
	public void publish(OtpErlangString topic, OtpErlangString content)
			throws ConnectionException {
		try {
			connection
					.sendRPC("pubsub.pubsub_api", "publish", new OtpErlangList(
							new OtpErlangObject[] { topic, content }));
			/**
             * The specification of {@code pubsub.pubsub_api:publish/2} states
             * that the only returned value is {@code ok}, so no further evaluation is
             * necessary.
			 */
			connection.receiveRPC();
			// OtpErlangObject received = connection.receiveRPC();
		} catch (OtpErlangExit e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (OtpAuthException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		}
	}
	
	/**
	 * Publishes an event under a given {@code topic}.
	 * 
	 * @param topic
	 *            the topic to publish the content under
	 * @param content
	 *            the content to publish
	 *
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 */
	public void publish(String topic, String content)
			throws ConnectionException {
		publish(new OtpErlangString(topic), new OtpErlangString(content));
	}

	// /////////////////////////////
	// subscribe methods
	// /////////////////////////////

	/**
	 * Subscribes a url to a {@code topic}.
	 * 
	 * @param topic
	 *            the topic to subscribe the url to
	 * @param url
	 *            the url of the subscriber (this is where the events are send
	 *            to)
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws UnknownException
	 *             if any other error occurs
	 */
	public void subscribe(OtpErlangString topic, OtpErlangString url) throws ConnectionException,
			TimeoutException, UnknownException {
		try {
			connection.sendRPC("pubsub.pubsub_api", "subscribe",
					new OtpErlangList(new OtpErlangObject[] { topic, url }));
			OtpErlangObject received = connection.receiveRPC();

			/*
			 * possible return values: - ok - {fail, not_found} - {fail,
			 * timeout} - {fail, fail} - {fail, abort}
			 */
			if (received.equals(new OtpErlangAtom("ok"))) {
				return;
			} else {
				// {fail, Reason}
				OtpErlangTuple returnValue = (OtpErlangTuple) received;

				if (returnValue.elementAt(1).equals(
						new OtpErlangAtom("timeout"))) {
					throw new TimeoutException();
				} else {
					throw new UnknownException(
							"Unknow error - erlang error message: "
									+ returnValue.toString());
				}
			}
		} catch (OtpErlangExit e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (OtpAuthException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}
	
	/**
	 * Subscribes a url to a {@code topic}.
	 * 
	 * @param topic
	 *            the topic to subscribe the url to
	 * @param url
	 *            the url of the subscriber (this is where the events are send
	 *            to)
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws UnknownException
	 *             if any other error occurs
	 */
	public void subscribe(String topic, String url) throws ConnectionException,
			TimeoutException, UnknownException {
		subscribe(new OtpErlangString(topic), new OtpErlangString(url));
	}
	
	// /////////////////////////////
	// unsubscribe methods
	// /////////////////////////////

	/**
	 * Unsubscribes a url from a {@code topic}.
	 * 
	 * @param topic
	 *            the topic to unsubscribe the url from
	 * @param url
	 *            the url of the subscriber
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws NotFoundException
	 *             if the topic does not exist or the given subscriber is not
	 *             subscribed to the given topic
	 * @throws UnknownException
	 *             if any other error occurs
	 */
	public void unsubscribe(OtpErlangString topic, OtpErlangString url)
			throws ConnectionException, TimeoutException, NotFoundException,
			UnknownException {
		try {
			connection.sendRPC("pubsub.pubsub_api", "unsubscribe",
					new OtpErlangList(new OtpErlangObject[] { topic, url }));
			OtpErlangObject received = connection.receiveRPC();

			/*
			 * possible return values: - ok - {fail, not_found} - {fail,
			 * timeout} - {fail, fail} - {fail, abort}
			 */
			if (received.equals(new OtpErlangAtom("ok"))) {
				return;
			} else {
				// {fail, Reason}
				OtpErlangTuple returnValue = (OtpErlangTuple) received;

				if (returnValue.elementAt(1).equals(
						new OtpErlangAtom("timeout"))) {
					throw new TimeoutException();
				} else if (returnValue.elementAt(1).equals(
						new OtpErlangAtom("not_found"))) {
					throw new NotFoundException();
				} else {
					throw new UnknownException(
							"Unknow error - erlang error message: "
									+ returnValue.toString());
				}
			}
		} catch (OtpErlangExit e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (OtpAuthException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}
	
	/**
	 * Unsubscribes a url from a {@code topic}.
	 * 
	 * @param topic
	 *            the topic to unsubscribe the url from
	 * @param url
	 *            the url of the subscriber
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws TimeoutException
	 *             if a timeout occurred while trying to write the value
	 * @throws NotFoundException
	 *             if the topic does not exist or the given subscriber is not
	 *             subscribed to the given topic
	 * @throws UnknownException
	 *             if any other error occurs
	 */
	public void unsubscribe(String topic, String url)
			throws ConnectionException, TimeoutException, NotFoundException,
			UnknownException {
		unsubscribe(new OtpErlangString(topic), new OtpErlangString(url));
	}

	// /////////////////////////////
	// utility methods
	// /////////////////////////////
	
	/**
	 * Converts the given erlang {@code list} of erlang strings to a Java {@link ArrayList}.
	 */
	private static ArrayList<String> erlStrListToStrArrayList(OtpErlangList list) {
		ArrayList<String> result = new ArrayList<String>(list.arity());
		for (int i = 0; i < list.arity(); ++i) {
			OtpErlangString elem = (OtpErlangString) list.elementAt(i);
			result.add(elem.stringValue());
		}
		return result;
	}

	// /////////////////////////////
	// get subscribers methods
	// /////////////////////////////
	
	/**
	 * Gets a list of subscribers to a {@code topic}.
	 * 
	 * @param topic
	 *            the topic to get the subscribers for
	 *
	 * @return the subscriber URLs
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws UnknownException
	 *             is thrown if the return type of the erlang method does not
	 *             match the expected one
	 */
	public OtpErlangList getSubscribers(
			OtpErlangString topic) throws ConnectionException, UnknownException {
		try {
			connection.sendRPC("pubsub.pubsub_api", "get_subscribers",
					new OtpErlangList(topic));
			// return value: [string,...]
			OtpErlangList received = (OtpErlangList) connection.receiveRPC();
			return received;
		} catch (OtpErlangExit e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (OtpAuthException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (IOException e) {
			// e.printStackTrace();
			throw new ConnectionException(e);
		} catch (ClassCastException e) {
			// e.printStackTrace();
			throw new UnknownException(e);
		}
	}
	
	/**
	 * Gets a list of subscribers to a {@code topic}.
	 * 
	 * @param topic
	 *            the topic to get the subscribers for
	 *
	 * @return the subscriber URLs
	 * 
	 * @throws ConnectionException
	 *             if the connection is not active or a communication error
	 *             occurs or an exit signal was received or the remote node
	 *             sends a message containing an invalid cookie
	 * @throws UnknownException
	 *             is thrown if the return type of the erlang method does not
	 *             match the expected one
	 */
	public ArrayList<String> getSubscribers(
			String topic) throws ConnectionException, UnknownException {
		return erlStrListToStrArrayList(getSubscribers(new OtpErlangString(topic)));
	}
	
	/**
	 * Closes the transaction's connection to a scalaris node.
	 * 
	 * Note: Subsequent calls to the other methods will throw
	 * {@link ConnectionException}s!
	 */
	public void closeConnection() {
		connection.close();
	}
}
