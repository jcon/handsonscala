import scala.concurrent._, ExecutionContext.Implicits.global, duration.Duration.Inf

def mergeSortParallel[T: Ordering](items: IndexedSeq[T]): IndexedSeq[T] = {
  Await.result(mergeSortParallel0(items), Inf)
}

def mergeSortParallel0[T: Ordering](items: IndexedSeq[T]): Future[IndexedSeq[T]] = {
  if (items.length <= 16) Future.successful(mergeSortSequential(items))
  else {
    val (left, right) = items.splitAt(items.length / 2)
    mergeSortParallel0(left).zip(mergeSortParallel0(right)).map{
      case (sortedLeft, sortedRight) => merge(sortedLeft, sortedRight)
    }
  }
}

def mergeSortSequential[T: Ordering](items: IndexedSeq[T]): IndexedSeq[T] = {
  if (items.length <= 1) items
  else {
    val (left, right) = items.splitAt(items.length / 2)
    merge(mergeSortSequential(left), mergeSortSequential(right))
  }
}

def merge[T: Ordering](sortedLeft: IndexedSeq[T], sortedRight: IndexedSeq[T]) = {
  var leftIdx = 0
  var rightIdx = 0
  val output = IndexedSeq.newBuilder[T]
  while (leftIdx < sortedLeft.length || rightIdx < sortedRight.length) {
    val takeLeft = (leftIdx < sortedLeft.length, rightIdx < sortedRight.length) match {
      case (true, false) => true
      case (false, true) => false
      case (true, true) => Ordering[T].lt(sortedLeft(leftIdx), sortedRight(rightIdx))
    }
    if (takeLeft) {
      output += sortedLeft(leftIdx)
      leftIdx += 1
    } else {
      output += sortedRight(rightIdx)
      rightIdx += 1
    }
  }
  output.result()
}
