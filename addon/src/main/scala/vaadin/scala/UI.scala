package vaadin.scala

import scala.collection.mutable
import vaadin.scala.internal.WrapperUtil
import vaadin.scala.internal.ClickListener
import vaadin.scala.internal.ListenersTrait
import vaadin.scala.mixins.AbstractComponentContainerMixin
import vaadin.scala.internal.WrappedVaadinUI
import scala.collection.mutable.ListBuffer

object UI {
  //def current: UI = com.vaadin.ui.UI.getCurrent
  //def current_=(root: Option[UI]): Unit = com.vaadin.ui.UI.setCurrent(root.getOrElse(null))
  //def current_=(root: UI): Unit = com.vaadin.ui.UI.setCurrent(root.p)
}

/**
 * @see com.vaadin.ui.UI
 * @author Henri Kerola / Vaadin
 */

abstract class UI(override val p: WrappedVaadinUI = new WrappedVaadinUI) extends AbstractComponentContainer(p) with DelayedInit {

  private var initCode: Option[() => Unit] = None

  def delayedInit(body: => Unit) {
    content = new VerticalLayout {
      margin = true
    }
    initCode = Some(() => body)
  }

  def doInit(request: ScaladinRequest) {
    for (proc <- initCode) proc()
    init(request)
  }

  def init(request: ScaladinRequest) {
    // This can be overridden in subclass if access to ScaladinRequest is needed in initialization code  
  }

  def uiId: Int = p.getUIId

  def windows: mutable.Set[Window] = new mutable.Set[Window] {
    import scala.collection.JavaConversions.asScalaIterator

    def contains(key: Window) = {
      p.getWindows.contains(key.p)
    }
    def iterator: Iterator[Window] = {
      p.getWindows.iterator.map(WrapperUtil.wrapperFor[Window](_).get) // TODO
    }
    def +=(elem: Window) = { p.addWindow(elem.p); this }
    def -=(elem: Window) = { p.removeWindow(elem.p); this }
  }

  def scrollIntoView(component: Component): Unit = p.scrollIntoView(component.p)

  def content: ComponentContainer = WrapperUtil.wrapperFor[ComponentContainer](p.getContent).get
  def content_=(content: ComponentContainer) = p.setContent(content.p)

  def resizeLazy: Boolean = p.isResizeLazy
  def resizeLazy_=(resizeLazy: Boolean): Unit = p.setResizeLazy(resizeLazy)
  // TODO: the same clickListeners can be found from Panel and Embedded, use a trait instead of copy-pasting? 
  lazy val clickListeners = new ListenersTrait[ClickEvent, ClickListener] {
    override def listeners = p.getListeners(classOf[com.vaadin.event.MouseEvents.ClickListener])
    override def addListener(elem: ClickEvent => Unit) = p.addClickListener(new ClickListener(elem))
    override def removeListener(elem: ClickListener) = p.removeClickListener(elem)
  }

  // TODO: setScrollTop

  // TODO: page

}